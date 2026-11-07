//! Minimal HTTP health probe used by the Docker image. It checks the real HTTP
//! listener rather than merely whether PID 1 exists, without requiring curl.
use std::io::{Read, Write};
use std::net::TcpStream;
use std::process::ExitCode;
use std::time::Duration;
use url::Url;

fn main() -> ExitCode {
    let target = std::env::args()
        .nth(1)
        .unwrap_or_else(|| format!("http://127.0.0.1:{}", default_port()));
    match check(&target) {
        Ok(()) => ExitCode::SUCCESS,
        Err(error) => {
            eprintln!("healthcheck failed: {}", error);
            ExitCode::FAILURE
        }
    }
}

fn check(target: &str) -> Result<(), String> {
    let target = parse_target(target)?;
    let mut stream = TcpStream::connect(&target.address).map_err(|e| e.to_string())?;
    stream
        .set_read_timeout(Some(Duration::from_secs(2)))
        .map_err(|e| e.to_string())?;
    stream
        .write_all(
            format!(
                "GET {} HTTP/1.1\r\nHost: {}\r\nConnection: close\r\n\r\n",
                target.path, target.host_header
            )
            .as_bytes(),
        )
        .map_err(|e| e.to_string())?;

    let mut response = [0_u8; 128];
    let read = stream.read(&mut response).map_err(|e| e.to_string())?;
    let response = String::from_utf8_lossy(&response[..read]).to_ascii_uppercase();
    let status = response
        .split_whitespace()
        .nth(1)
        .ok_or_else(|| format!("invalid HTTP response: {}", response.trim()))?;
    if status == "200" {
        Ok(())
    } else {
        Err(format!("unexpected HTTP status {}", status))
    }
}

fn default_port() -> u16 {
    std::env::var("YT_SERVER_PORT")
        .ok()
        .and_then(|port| port.parse().ok())
        .unwrap_or(4007)
}

#[derive(Debug, PartialEq, Eq)]
struct HttpTarget {
    address: String,
    host_header: String,
    path: String,
}

fn parse_target(value: &str) -> Result<HttpTarget, String> {
    let url = Url::parse(value).map_err(|e| format!("invalid healthcheck URL {value}: {e}"))?;
    if url.scheme() != "http" {
        return Err(format!("healthcheck URL must use http: {value}"));
    }
    let host = url
        .host_str()
        .ok_or_else(|| format!("healthcheck URL has no host: {value}"))?;
    let port = url
        .port_or_known_default()
        .ok_or_else(|| format!("healthcheck URL has no port: {value}"))?;
    let address = if host.contains(':') {
        format!("[{host}]:{port}")
    } else {
        format!("{host}:{port}")
    };
    let host_header = if port == 80 {
        host.to_owned()
    } else {
        format!("{host}:{port}")
    };
    let mut path = url.path().to_owned();
    if path.is_empty() {
        path.push('/');
    }
    if let Some(query) = url.query() {
        path.push('?');
        path.push_str(query);
    }
    Ok(HttpTarget {
        address,
        host_header,
        path,
    })
}

#[cfg(test)]
mod tests {
    use super::parse_target;

    #[test]
    fn parses_default_http_target() {
        let target = parse_target("http://127.0.0.1:4007/").unwrap();
        assert_eq!(target.address, "127.0.0.1:4007");
        assert_eq!(target.host_header, "127.0.0.1:4007");
        assert_eq!(target.path, "/");
    }

    #[test]
    fn preserves_path_and_query() {
        let target = parse_target("http://localhost:8080/ready?probe=1").unwrap();
        assert_eq!(target.address, "localhost:8080");
        assert_eq!(target.host_header, "localhost:8080");
        assert_eq!(target.path, "/ready?probe=1");
    }
}
