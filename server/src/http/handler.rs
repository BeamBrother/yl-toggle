use super::{cors_headers, ClientParams, SdkAuthorization, ToggleUpdateParams};
#[cfg(feature = "unstable")]
use super::{SecretsParams, SegmentUpdateParams};
use crate::YTServerError::{NotFound, NotReady};
use crate::{events_buffer::EventsStats, repo::SdkRepository, YTServerError};
use axum::{
    async_trait,
    extract::Query,
    http::{HeaderMap, HeaderValue, StatusCode},
    response::{IntoResponse, Response},
    Json, TypedHeader,
};
use parking_lot::Mutex;
use reqwest::header::{self, AUTHORIZATION, USER_AGENT};
use serde_json::Value;
use std::{
    collections::{HashMap, VecDeque},
    fs,
    path::PathBuf,
    sync::Arc,
};
use yl_toggle_event::collector::{EventHandler, YTEventError};
use yl_toggle_server_sdk::{Repository, SyncType, YTUser};

#[async_trait]
pub trait HttpHandler {
    async fn server_sdk_toggles(
        &self,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError>;

    async fn client_sdk_toggles(
        &self,
        Query(params): Query<ClientParams>,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError>;

    async fn client_sdk_events(
        &self,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError>;

    async fn events_stats(&self) -> Result<Json<EventsStats>, YTServerError>;

    #[cfg(not(feature = "unstable"))]
    async fn update_toggles(
        &self,
        Json(params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError>;

    #[cfg(feature = "unstable")]
    async fn update_toggles(
        &self,
        Json(params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError>;

    #[cfg(feature = "unstable")]
    async fn update_segments(
        &self,
        Json(params): Json<SegmentUpdateParams>,
    ) -> Result<Response, YTServerError>;

    #[cfg(feature = "unstable")]
    async fn check_secrets(
        &self,
        Json(_params): Json<SecretsParams>,
    ) -> Result<Json<HashMap<String, String>>, YTServerError>;

    async fn all_secrets(&self) -> Result<Json<HashMap<String, String>>, YTServerError>;
}

#[derive(Clone)]
pub struct FpHttpHandler {
    pub repo: Arc<SdkRepository>,
    pub prerequisite_deep: u8,
    pub events_buffer: Arc<crate::events_buffer::EventsBuffer>,
}

#[async_trait]
impl HttpHandler for FpHttpHandler {
    async fn server_sdk_toggles(
        &self,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        match self.repo.server_sdk_repo_string(&sdk_key) {
            Ok(body) => Ok((
                StatusCode::OK,
                [(header::CONTENT_TYPE, "application/json")],
                body,
            )
                .into_response()),
            Err(e) => match e {
                NotReady(_) => Ok((
                    StatusCode::SERVICE_UNAVAILABLE,
                    [(header::CONTENT_TYPE, "application/json")],
                    "{}",
                )
                    .into_response()),
                NotFound(_) => Ok((
                    StatusCode::OK,
                    [(header::CONTENT_TYPE, "application/json")],
                    serde_json::to_string(&Repository::default()).unwrap(),
                )
                    .into_response()),
                _ => Err(e),
            },
        }
    }

    async fn client_sdk_toggles(
        &self,
        Query(params): Query<ClientParams>,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        let user = decode_user(params.user)?;
        let prerequisite_deep = self.prerequisite_deep;
        match self
            .repo
            .client_sdk_eval_string(&sdk_key, &user, prerequisite_deep)
        {
            Ok(body) => Ok((StatusCode::OK, cors_headers(), body).into_response()),
            Err(e) => match e {
                NotReady(_) => {
                    Ok((StatusCode::SERVICE_UNAVAILABLE, cors_headers(), "{}").into_response())
                }
                NotFound(_) => Ok((StatusCode::OK, cors_headers(), "{}").into_response()),
                _ => Err(e),
            },
        }
    }

    async fn client_sdk_events(
        &self,
        TypedHeader(SdkAuthorization(sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        match self.repo.client_sdk_events_string(&sdk_key) {
            Ok(body) => Ok((StatusCode::OK, cors_headers(), body).into_response()),
            Err(e) => match e {
                NotReady(_) => {
                    Ok((StatusCode::SERVICE_UNAVAILABLE, cors_headers(), "{}").into_response())
                }
                NotFound(_) => Ok((StatusCode::OK, cors_headers(), "{}").into_response()),
                _ => Err(e),
            },
        }
    }

    async fn events_stats(&self) -> Result<Json<EventsStats>, YTServerError> {
        Ok(Json(self.events_buffer.stats()))
    }

    #[cfg(not(feature = "unstable"))]
    async fn update_toggles(
        &self,
        Json(params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError> {
        let sdk_key = params.sdk_key;
        match self.repo.client_sync_now(&sdk_key, SyncType::Realtime) {
            Ok(_sdk_key) => Ok((StatusCode::OK, cors_headers(), "{}").into_response()),
            Err(e) => match e {
                NotFound(_) => Ok((
                    StatusCode::BAD_REQUEST,
                    [(header::CONTENT_TYPE, "application/json")],
                    "{}",
                )
                    .into_response()),
                _ => Err(e),
            },
        }
    }

    #[cfg(feature = "unstable")]
    async fn update_toggles(
        &self,
        Json(params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError> {
        self.repo.update_toggles(&params.sdk_key, params.toggles)?;
        let status = StatusCode::OK;
        Ok(status.into_response())
    }
    #[cfg(feature = "unstable")]

    async fn update_segments(
        &self,
        Json(params): Json<SegmentUpdateParams>,
    ) -> Result<Response, YTServerError> {
        self.repo.update_segments(params.segments)?;
        let status = StatusCode::OK;
        let body = "";
        Ok((status, body).into_response())
    }

    #[cfg(feature = "unstable")]
    async fn check_secrets(
        &self,
        Json(_params): Json<SecretsParams>,
    ) -> Result<Json<HashMap<String, String>>, YTServerError> {
        Ok(HashMap::new().into())
    }

    async fn all_secrets(&self) -> Result<Json<HashMap<String, String>>, YTServerError> {
        let secret_keys = self.repo.secret_keys();
        Ok(secret_keys.into())
    }
}

#[async_trait]
impl EventHandler for FpHttpHandler {
    async fn handle_events(
        &self,
        sdk_key: String,
        user_agent: String,
        headers: HeaderMap,
        data: VecDeque<Value>,
    ) -> Result<Response, YTEventError> {
        let server_sdk_key = {
            if sdk_key.starts_with("client") {
                match self.repo.secret_keys().get(sdk_key.as_str()) {
                    Some(key) => key.clone(),
                    None => {
                        return Ok((StatusCode::UNAUTHORIZED, cors_headers(), "{}").into_response())
                    }
                }
            } else {
                sdk_key.clone()
            }
        };
        let headers = build_header(headers, server_sdk_key, &user_agent);
        self.events_buffer.push(headers, data);
        Ok((StatusCode::OK, cors_headers(), "{}").into_response())
    }
}

fn build_header(headers: HeaderMap, sdk_key: String, user_agent: &str) -> HeaderMap {
    let ua = headers.get("ua");
    let auth = SdkAuthorization(sdk_key).encode();
    let mut headers = HeaderMap::with_capacity(3);
    headers.append(AUTHORIZATION, auth);
    if let Ok(v) = HeaderValue::from_str(user_agent) {
        headers.append(USER_AGENT, v);
    }

    if let Some(ua) = ua {
        headers.append("ua", ua.clone());
    }
    headers
}

fn decode_user(user: String) -> Result<YTUser, YTServerError> {
    if let Ok(user) = base64::decode(user) {
        if let Ok(user) = String::from_utf8(user) {
            if let Ok(user) = serde_json::from_str::<YTUser>(&user) {
                return Ok(user);
            }
        }
    }
    Err(YTServerError::UserDecodeError)
}

// used for mocking yl-toggle API
#[derive(Clone, Default)]
pub struct LocalFileHttpHandlerForTest {
    pub version_update: bool,
    body: Arc<Mutex<Option<String>>>,
}

#[async_trait]
impl HttpHandler for LocalFileHttpHandlerForTest {
    async fn server_sdk_toggles(
        &self,
        TypedHeader(SdkAuthorization(_sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        let mut lock = self.body.lock();
        let body = match &*lock {
            None => {
                let mut path = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
                path.push("resources/fixtures/repo.json");
                let body = fs::read_to_string(path).unwrap();
                *lock = Some(body.clone());
                body
            }
            Some(body) => body.clone(),
        };

        if self.version_update {
            // SAFETY: json is valid
            let mut repo: Repository = serde_json::from_str(&body).unwrap();
            let version = repo.version.unwrap_or_default();
            repo.version = Some(version + 1);
            // SAFETY: repo charset valid
            *lock = Some(serde_json::to_string(&repo).unwrap());
        }

        Ok((
            StatusCode::OK,
            [(header::CONTENT_TYPE, "application/json")],
            body,
        )
            .into_response())
    }

    async fn client_sdk_toggles(
        &self,
        Query(_params): Query<ClientParams>,
        TypedHeader(SdkAuthorization(_sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        let body = "{}".to_owned();
        Ok((
            StatusCode::OK,
            [(header::CONTENT_TYPE, "application/json")],
            body,
        )
            .into_response())
    }

    async fn client_sdk_events(
        &self,
        TypedHeader(SdkAuthorization(_sdk_key)): TypedHeader<SdkAuthorization>,
    ) -> Result<Response, YTServerError> {
        let body = "{}".to_owned();
        Ok((
            StatusCode::OK,
            [(header::CONTENT_TYPE, "application/json")],
            body,
        )
            .into_response())
    }

    async fn events_stats(&self) -> Result<Json<EventsStats>, YTServerError> {
        Ok(Json(EventsStats::default()))
    }

    #[cfg(not(feature = "unstable"))]
    async fn update_toggles(
        &self,
        Json(_params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError> {
        let body = "{}".to_owned();
        Ok((
            StatusCode::OK,
            [(header::CONTENT_TYPE, "application/json")],
            body,
        )
            .into_response())
    }

    #[cfg(feature = "unstable")]
    async fn update_toggles(
        &self,
        Json(_params): Json<ToggleUpdateParams>,
    ) -> Result<Response, YTServerError> {
        let body = "{}".to_owned();
        Ok((
            StatusCode::OK,
            [(header::CONTENT_TYPE, "application/json")],
            body,
        )
            .into_response())
    }

    #[cfg(feature = "unstable")]
    async fn update_segments(
        &self,
        Json(_params): Json<SegmentUpdateParams>,
    ) -> Result<Response, YTServerError> {
        let status = StatusCode::OK;
        let body = "";
        Ok((status, body).into_response())
    }

    #[cfg(feature = "unstable")]
    async fn check_secrets(
        &self,
        Json(_params): Json<SecretsParams>,
    ) -> Result<Json<HashMap<String, String>>, YTServerError> {
        Ok(HashMap::new().into())
    }

    async fn all_secrets(&self) -> Result<Json<HashMap<String, String>>, YTServerError> {
        let mut path = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
        path.push("resources/fixtures/secrets.json");
        let json_str = fs::read_to_string(path).unwrap();
        let secret_keys =
            serde_json::from_str::<HashMap<String, HashMap<String, String>>>(&json_str).unwrap();
        let secret_keys = secret_keys.get("mapping").unwrap().to_owned();
        Ok(secret_keys.into())
    }
}

#[async_trait]
impl EventHandler for LocalFileHttpHandlerForTest {
    async fn handle_events(
        &self,
        _sdk_key: String,
        _user_agent: String,
        _headers: HeaderMap,
        _data: VecDeque<Value>,
    ) -> Result<Response, YTEventError> {
        Ok((StatusCode::OK, cors_headers(), "").into_response())
    }
}
