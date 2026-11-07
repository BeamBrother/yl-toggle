use std::fs::{remove_file, File};
use std::io::prelude::*;
#[cfg(target_family = "unix")]
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};

#[cfg(target_family = "unix")]
fn create_file(path: PathBuf, make_executable: bool) -> Result<File, ()> {
    let file = match File::create(&path) {
        Ok(file) => file,
        Err(_) => return Err(()),
    };

    if make_executable {
        let metadata = match file.metadata() {
            Ok(metadata) => metadata,
            Err(_) => return Err(()),
        };
        let mut permissions = metadata.permissions();
        permissions.set_mode(0o755);
        if std::fs::set_permissions(&path, permissions).is_err() {
            return Err(());
        };
    };

    Ok(file)
}

#[cfg(target_family = "windows")]
fn create_file(path: PathBuf, _make_executable: bool) -> Result<File, ()> {
    match File::create(&path) {
        Err(_) => Err(()),
        Ok(file) => Ok(file),
    }
}

pub fn get_file_writer(
) -> fn(file_path: &str, contents: &str, make_executable: bool) -> Result<(), String> {
    |file_path: &str, contents: &str, make_executable: bool| {
        let path = PathBuf::from(file_path);
        let mut file = match create_file(path, make_executable) {
            Ok(f) => f,
            Err(_) => return Err(format!("Failed to create file {}", file_path)),
        };

        match file.write_all(contents.as_bytes()) {
            Ok(_) => Ok(()),
            Err(_) => Err(format!("Failed to write contents to {}", file_path)),
        }
    }
}

pub fn get_file_existence_checker() -> fn(file_path: &str) -> Result<bool, ()> {
    |file_path: &str| Ok(Path::new(file_path).exists())
}

pub fn get_file_reader() -> fn(file_path: &str) -> Result<String, ()> {
    |file_path: &str| {
        let mut file = match File::open(&Path::new(file_path)) {
            Err(_) => return Err(()),
            Ok(file) => file,
        };
        let mut contents = String::new();
        match file.read_to_string(&mut contents) {
            Err(_) => Err(()),
            Ok(_) => Ok(contents),
        }
    }
}

pub fn get_file_remover() -> fn(file_path: &str) -> Result<(), String> {
    |file_path: &str| match remove_file(file_path) {
        Ok(_) => Ok(()),
        Err(msg) => Err(format!(
            "Failed to remove file: {}. Error details: {}",
            file_path, msg
        )),
    }
}

#[cfg(test)]
#[path = "file_io_test.rs"]
mod file_io_tests;
