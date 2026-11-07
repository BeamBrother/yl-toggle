INSERT INTO `member` (id, account, password, role, deleted, visited_time, modified_time, created_by, created_time, modified_by) VALUES (-1,'manager@yl-toggle.com','$2a$10$WO5tC7A/nsPe5qmVmjTIPeKD0R/Tm2YsNiVP0geCerT0hIRLBCxZ6','ADMIN',0,now(),now(), -1 ,now(), -1);


INSERT INTO organize(id, name, modified_time, created_by, created_time, modified_by) VALUES (-1, "Manager Organize", now(), -1, now(), -1);


insert into organize_user(organize_id, user_id, role, modified_time, created_by, created_time, modified_by) VALUES (-1, -1, "OWNER", now(), -1, now(), -1);


INSERT INTO `project` (`organize_id`, `key`, `name`, `description`, `deleted`, `modified_time`, `created_by`, `created_time`, `modified_by`)
VALUES (-1, 'yl-toggle', 'yl-toggle', '', 0, now(), -1, now(), -1);

INSERT INTO `environment` (`organize_id`, `name`, `key`, `project_key`, `server_sdk_key`, `client_sdk_key`, `deleted`, `modified_time`, `created_by`, `created_time`, `modified_by`) VALUES (-1, 'online', 'online', 'yl-toggle', 'server-t6h78815ef044428826787e9a238b9c6a479f998', 'client-29765c7e03e9cb49c0e96357b797b1e47e7f2dee', 0, now(), -1, now(), -1);

INSERT INTO `toggle` (`organize_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, `variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle project limiter', 'yl-toggle_project_limiter', '', 'number', 0, '[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"2\",\"name\":\"max\",\"description\":\"\"}]', 'yl-toggle', 0, 0, 0, -1, -1, now(), now());

INSERT INTO `targeting` (`organize_id`, `toggle_key`, `environment_key`, `project_key`, `version`, `disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle_project_limiter', 'online', 'yl-toggle', 2, 1, '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"2\",\"name\":\"max\",\"description\":\"\"}]}', 0, -1, -1, now(), now());


INSERT INTO `toggle` (`organize_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, `variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle env limiter', 'yl-toggle_env_limiter', '', 'number', 0, '[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"5\",\"name\":\"max\",\"description\":\"\"}]', 'yl-toggle', 0, 0, 0,-1, -1, now(), now());

INSERT INTO `targeting` (`organize_id`, `toggle_key`, `environment_key`, `project_key`, `version`, `disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle_env_limiter', 'online', 'yl-toggle', 3, 1, '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"5\",\"name\":\"max\",\"description\":\"\"}]}', 0, -1, -1, now(), now());


INSERT INTO `toggle` (`organize_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, `variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle toggle limiter', 'yl-toggle_toggle_limiter', '', 'number', 0, '[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"10\",\"name\":\"max\",\"description\":\"\"}]', 'yl-toggle', 0, 0, 0, -1, -1, now(), now());


INSERT INTO `targeting` (`organize_id`, `toggle_key`, `environment_key`, `project_key`, `version`, `disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'yl-toggle_toggle_limiter', 'online', 'yl-toggle', 3, 1, '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"-1\",\"name\":\"unlimited\",\"description\":\"\"},{\"value\":\"10\",\"name\":\"max\",\"description\":\"\"}]}', 0, -1, -1, now(), now());


INSERT INTO `toggle` (`organize_id`, `name`, `key`, `description`, `return_type`, `disabled_serve`, `variations`, `project_key`, `archived`, `client_availability`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'Control yl-toggle Demo Features', 'demo_features', 'Control yl-toggle Demo Features Hide or Show', 'boolean', 0, '[{\"value\":\"false\",\"name\":\"Open Source\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"Demo\",\"description\":\"\"}]', 'yl-toggle', 0, 1, 0, -1, -1, now(), now());

INSERT INTO `targeting` (`organize_id`, `toggle_key`, `environment_key`, `project_key`, `version`, `disabled`, `content`, `deleted`, `modified_by`, `created_by`, `created_time`, `modified_time`)
VALUES (-1, 'demo_features', 'online', 'yl-toggle', 2, 1, '{\"rules\":[],\"disabledServe\":{\"select\":0},\"defaultServe\":{\"select\":0},\"variations\":[{\"value\":\"false\",\"name\":\"Open Source\",\"description\":\"\"},{\"value\":\"true\",\"name\":\"Demo\",\"description\":\"\"}]}', 0, -1, -1, now(), now());