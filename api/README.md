# yl-toggle API

[![codecov](https://codecov.io/gh/yl-toggle/yl-toggle/api/branch/main/graph/badge.svg?token=JVJWQUUIJH)](https://app.codecov.io/github/yl-toggle/yl-toggle/tree/main/api)
[![Docker Pulls](https://img.shields.io/docker/pulls/yl-toggle/api)](https://hub.docker.com/u/yl-toggle)
[![Apache-2.0 license](https://img.shields.io/github/license/yl-toggle/yl-toggle)](https://github.com/yl-toggle/yl-toggle/blob/main/LICENSE)

yl-toggle supports an extensive API so that other services or clients can use it programmatically.
This API is exposed using the standard REST architecture in a secure, uniform, stateless manner.

yl-toggle API is one of the key components of the whole system. You must install it to set up yl-toggle services.
We provide a docker composer configuration as an example to make it easier to set up all the services together including
API;
or you can choose to run API as a docker container and boot other services one by one.


## [中文文档](https://docs.yl-toggle.io/zh-CN/)

## Getting Started

### Using Docker Composer to Install Main Services

We recommend booting up UI along with other core components by using a docker composer file.

Here is an example to help to boot up yl-toggle Server, API, UI and db with a simple docker-compose up command.
Check it out at [yl-toggle Official Compose File](https://github.com/yl-toggle/yl-toggle).

Or you can simply run below command to clone and boot up the Docker composer components.

``` bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker compose up
```

### Installing API Independently with a Docker Image

You can alternatively install and run API with a Docker image. To run, binding the exposed port 4007 to the host, use:

```
$ docker run -d -p 4007:4007 --name fp-api yl-toggle/api
```

Remember to add the environment variables for connecting an RDS database, like

```angular2svg
spring.datasource.jdbc-url=jdbc:mysql://database:13306/yl-toggle?useSSL=false&characterEncoding=utf8&autoReconnect=true&failOverReadOnly=false&rewriteBatchedStatements=TRUE&useSSL=false&serverTimezone=Asia/Shanghai
```

### Building jar Package and Running it on a VM or Server

You can also choose to build the project and generate the package use Maven.

```
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api

mvn clean package
```

If everything checks out, the jar package should be available in the target folder.
You can deploy it as you wish to.

```
java -jar yl-toggle-api-1.0.0.jar
```

### Building your own Image and Installing

In most cases, the settings from the environment are used to populate the application configuration on the container
startup.
However, in some cases you may wish to customise the settings in ways that are not supported by the environment
variables above.
There are two main ways of doing this; modify our repository to your own image, build a new image from the existing one.
We will briefly outline these methods here, but in practice how you do this will depend on your needs.

#### Building your own image

* Clone the yl-toggle API repository at https://github.com/yl-toggle/yl-toggle.git 
* Modify codes as you wish.
* Build the new image with e.g: docker build --tag my-yl-toggle-api
* Optionally push to a registry, and deploy.

#### Build a new image from the existing one

* Create a new Dockerfile, which starts with the line e.g: FROM yl-toggle/api:latest.
* Add some commands.
* Build, push and deploy the new image as above.

## API Endpoints

Checkout the API endpoints at `http://localhost:4009/api-docs` after you boot up the service locally with the
Docker Composer file.
You may need to modify the host address and port if you deploy the services in a different way.

## Contributing

We are working on continue evolving yl-toggle core, making it flexible and easier to use.
Development of yl-toggle happens in the open on GitHub, and we are grateful to the
community for contributing bugfixes and improvements.

Please read [CONTRIBUTING](https://github.com/yl-toggle/yl-toggle/blob/master/CONTRIBUTING.md)
for details on our code of conduct, and the process for taking part in improving yl-toggle.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.

