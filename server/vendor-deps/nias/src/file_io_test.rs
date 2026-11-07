use super::*;

#[cfg(test)]
mod get_file_writer_tests {
    use super::*;

    #[test]
    fn returns_correct_closure() {
        fn validate(_write_file: fn(&str, &str, bool) -> Result<(), String>) -> bool {
            true
        };
        let writer = get_file_writer();
        assert!(validate(writer));
    }
}

#[cfg(test)]
mod get_file_existence_checker_tests {
    use super::*;

    #[test]
    fn returns_correct_closure() {
        fn validate(_exists: fn(&str) -> Result<bool, ()>) -> bool {
            true
        };
        let file_exists = get_file_existence_checker();
        assert!(validate(file_exists));
    }
}

#[cfg(test)]
mod get_file_reader_tests {
    use super::*;

    #[test]
    fn returns_correct_closure() {
        fn validate(_read: fn(&str) -> Result<String, ()>) -> bool {
            true
        };
        let read_file = get_file_reader();
        assert!(validate(read_file));
    }
}

#[cfg(test)]
mod get_file_remover_tests {
    use super::*;

    #[test]
    fn returns_correct_closure() {
        fn validate(_remove_file: fn(&str) -> Result<(), String>) -> bool {
            true
        };
        let remover = get_file_remover();
        assert!(validate(remover));
    }
}
