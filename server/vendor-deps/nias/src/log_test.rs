use super::*;

#[cfg(test)]
mod log_tests {
    use super::*;

    #[test]
    fn writes_to_buffer() {
        let mut writer = Vec::new();
        let message = "winning";
        log(&mut writer, message);
        assert_eq!(&writer[..], "winning\n".as_bytes());
    }
}

#[cfg(test)]
mod log_conditionally_tests {
    use super::*;

    #[test]
    fn writes_to_buffer_when_should_log_true() {
        let mut writer = Vec::new();
        let message = "test result: ok. 8 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out";
        let exp = &format!("{}\n", message);
        log_conditionally(&mut writer, message, true);
        assert_eq!(&writer[..], exp.as_bytes());
    }

    #[test]
    fn does_not_write_to_buffer_when_should_log_false() {
        let mut writer = Vec::new();
        let message = "do not log me!!!";
        log_conditionally(&mut writer, message, false);
        assert_eq!(&writer[..], "".as_bytes());
    }
}

#[cfg(test)]
mod get_conditional_logger_tests {
    use super::*;

    #[test]
    fn returns_correct_closure() {
        fn validate(_log_conditionally: fn(&str, bool)) -> bool {
            true
        };
        let logger = get_conditional_logger();
        logger("", false);
        assert!(validate(logger));
    }
}
