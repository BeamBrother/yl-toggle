use super::*;
use std::process::ExitStatus;

const RAW_STDOUT_OUTPUT: &str = "hello\nworld!\n\n";
const EXP_OUTPUT: &str = "hello\nworld!";

#[cfg(target_family = "windows")]
fn get_exit_status(raw: u32) -> ExitStatus {
    use std::os::windows::process::ExitStatusExt;
    ExitStatus::from_raw(raw)
}

#[cfg(target_family = "unix")]
fn get_exit_status(raw: u32) -> ExitStatus {
    use std::os::unix::process::ExitStatusExt;
    ExitStatus::from_raw(raw as i32)
}

#[cfg(test)]
mod get_command_runner_tests {
    use super::get_command_runner;

    #[test]
    fn returns_correct_closure() {
        fn validate(
            _run_command: fn(&str, Option<&str>, bool) -> Result<Option<String>, Option<String>>,
        ) -> bool {
            true
        };
        let command_runner = get_command_runner();
        assert!(validate(command_runner));
    }
}

#[cfg(test)]
mod handle_command_output_tests {
    use super::*;
    use std::io::{Error, ErrorKind};
    use std::process::Output;

    #[test]
    #[should_panic(
        expected = "Command runner crashed in unrecoverable manner. Details: crashed hard"
    )]
    fn should_panic_on_command_error() {
        let error_details = "crashed hard";
        let error = Error::new(ErrorKind::Other, error_details);
        let _ = handle_command_output(Err(error));
    }

    #[test]
    fn should_return_ok_with_stdout_on_command_success() {
        let output = Output {
            status: get_exit_status(0),
            stdout: Vec::from(RAW_STDOUT_OUTPUT.as_bytes()),
            stderr: Vec::new(),
        };
        let result = handle_command_output(Ok(output));
        assert_eq!(result.unwrap(), Some(String::from(EXP_OUTPUT)));
    }

    #[test]
    fn should_return_err_with_stdout_stderr_mix_on_command_failure() {
        let std_error = "utter failure\n";
        let output = Output {
            status: get_exit_status(3),
            stdout: Vec::from(RAW_STDOUT_OUTPUT.as_bytes()),
            stderr: Vec::from(std_error.as_bytes()),
        };
        let result = handle_command_output(Ok(output));
        assert_eq!(
            result.unwrap_err(),
            Some(format!("{}\n{}", std_error, RAW_STDOUT_OUTPUT))
        );
    }
}

#[cfg(test)]
mod handle_streamed_command_tests {
    use super::*;
    use std::io::{Error, ErrorKind};

    #[test]
    #[should_panic(expected = "Command runner crashed in unrecoverable manner. Details: oops!")]
    fn should_panic_on_command_error() {
        let error_details = "oops!";
        let error = Error::new(ErrorKind::Other, error_details);
        let _ = handle_streamed_command(Err(error));
    }

    #[test]
    fn should_return_ok_on_command_success() {
        let result = handle_streamed_command(Ok(get_exit_status(0)));
        assert_eq!(result.unwrap(), None);
    }

    #[test]
    fn should_return_err_on_command_failure() {
        let result = handle_streamed_command(Ok(get_exit_status(3)));
        assert_eq!(result.unwrap_err(), None);
    }
}
