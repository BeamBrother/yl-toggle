use std::io::Write;

fn log<W: Write>(writer: &mut W, message: &str) {
    writeln!(writer, "{}", message).unwrap_or_default();
}

fn log_conditionally<W: Write>(writer: &mut W, message: &str, should_log: bool) {
    if should_log {
        log(writer, message);
    }
}

pub fn get_conditional_logger() -> fn(message: &str, should_log: bool) {
    |message: &str, should_log: bool| {
        log_conditionally(&mut std::io::stdout(), message, should_log);
    }
}

#[cfg(test)]
#[path = "log_test.rs"]
mod log_tests;
