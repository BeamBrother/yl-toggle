# yl-toggle UI


yl-toggle applications suite includes a customer UI/Portal. It is optional to install,
but we highly recommend using it as the management portal.

The yl-toggle UI provides extensive UI features for managing and updating properties,
rollout, rollback, configuration peer reviews, permission management, history, and many other features.

## [中文文档](https://docs.yl-toggle.io/zh-CN/)

## Getting Started


We provide an off-the-shelf UI for yl-toggle.
You can boot up it within a minute by running the Docker-compose file or the Docker Image we provide here.
You can also customize the original UI with your own idea,
or even build your own UI and connect it with yl-toggle API. Here are several approaches to set up a UI.


### Using Docker Composer to Install UI and other Main Services Together

We recommend booting up UI along with other core components by using a docker composer file.

Here is an example to help to boot up yl-toggle Server, API, UI and db with a simple docker-compose up command.
Check it out at [yl-toggle Official Compose File](https://github.com/yl-toggle/yl-toggle).

Or you can simply run below command to clone and boot up the Docker composer components.
``` bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker compose up
```


### Installing UI Independently with a Docker Image

The image serves a static portal preview on port 4009. For a connected deployment, mount an NGINX configuration that proxies the API and evaluation endpoints, as shown in the [independent Docker deployment guide](https://docs.yl-toggle.io/how-to/deploy/deployment-docker-image).
```bash
docker run -d -p 4009:4009 --name yl-toggle-ui yl-toggle/ui
```

### Installing UI Independently with npm

UI development uses npm and the committed package lock. See [How to install UI](https://docs.yl-toggle.io/how-to/develop/ui-local-develop).
[中文版](https://docs.yl-toggle.io/zh-CN/how-to/develop/ui-local-develop)


### Building a customized UI on top of the original one
If you would like to customize the original UI of yl-toggle, you can clone or folk a copy of this repository
and do your own coding and building.


## Contributing
We are working on continue evolving yl-toggle core, making it flexible and easier to use.
Development of yl-toggle happens in the open on GitHub, and we are grateful to the
community for contributing bugfixes and improvements.

Please read [CONTRIBUTING](https://github.com/yl-toggle/yl-toggle/blob/master/CONTRIBUTING.md)
for details on our code of conduct, and the process for taking part in improving yl-toggle.


## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
