use std::io::Result as IOResult;
use std::process::{Command, ExitStatus, Output};

#[cfg(target_family = "unix")]
const PROGRAM: &str = "sh";
#[cfg(target_family = "unix")]
const SWITCH: &str = "-c";

#[cfg(target_family = "windows")]
const PROGRAM: &str = "cmd";
#[cfg(target_family = "windows")]
const SWITCH: &str = "/C";

fn handle_command_output(output: IOResult<Output>) -> Result<Option<String>, Option<String>> {
    match output {
        Err(details) => panic!(
            "Command runner crashed in unrecoverable manner. Details: {}",
            details
        ),
        Ok(output) => {
            if output.status.success() {
                Ok(Some(
                    String::from_utf8(output.stdout)
                        .unwrap()
                        .trim_end_matches('\n')
                        .to_string(),
                ))
            } else {
                Err(Some(format!(
                    "{}\n{}",
                    String::from_utf8(output.stderr).unwrap(),
                    String::from_utf8(output.stdout).unwrap(),
                )))
            }
        }
    }
}

fn handle_streamed_command(
    exit_status: IOResult<ExitStatus>,
) -> Result<Option<String>, Option<String>> {
    match exit_status {
        Err(details) => panic!(
            "Command runner crashed in unrecoverable manner. Details: {}",
            details
        ),
        Ok(status) if status.success() => Ok(None),
        Ok(_) => Err(None),
    }
}

pub fn get_command_runner(
) -> fn(cmd: &str, dir: Option<&str>, stream_io: bool) -> Result<Option<String>, Option<String>> {
    |cmd: &str, dir: Option<&str>, stream_io: bool| {
        let target_dir = match dir {
            Some(d) => d,
            None => ".",
        };

        if stream_io {
            handle_streamed_command(
                Command::new(PROGRAM)
                    .current_dir(target_dir)
                    .args(&[SWITCH, cmd])
                    .status(),
            )
        } else {
            handle_command_output(
                Command::new(PROGRAM)
                    .current_dir(target_dir)
                    .args(&[SWITCH, cmd])
                    .output(),
            )
        }
    }
}

#[cfg(test)]
#[path = "command_test.rs"]
mod command_tests;
