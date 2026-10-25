//! Minimal HTTP health probe used by the Docker image. It checks the real HTTP
//! listener rather than merely whether PID 1 exists, without requiring curl.
use std::io::{Read, Write};
use std::net::TcpStream;
use std::process::ExitCode;
use std::time::Duration;

fn main() -> ExitCode {
    match check() {
        Ok(()) => ExitCode::SUCCESS,
        Err(error) => {
            eprintln!("healthcheck failed: {}", error);
            ExitCode::FAILURE
        }
    }
}

fn check() -> Result<(), String> {
    let port = std::env::var("YT_SERVER_PORT")
        .ok()
        .and_then(|port| port.parse().ok())
        .unwrap_or(4007);
    let address = format!("127.0.0.1:{}", port);
    let mut stream = TcpStream::connect(&address).map_err(|e| e.to_string())?;
    stream
        .set_read_timeout(Some(Duration::from_secs(2)))
        .map_err(|e| e.to_string())?;
    stream
        .write_all(b"GET / HTTP/1.1\r\nHost: 127.0.0.1\r\nConnection: close\r\n\r\n")
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
