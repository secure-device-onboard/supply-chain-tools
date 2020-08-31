**NOTE**: Supply Chain Tools demo docker scripts are provided solely to demonstrate the interoperation of Supply Chain Tools with an example database and configuration.  _These scripts are not recommended for use in any production capacity._  Appropriate security measures with respect to key-store management and configuration management should be considered while performing production deployment of any Secure Device Onboard component.

# Manufacturer Toolkit Docker Configuration

## System Requirements

* Operating system: **Linux Ubuntu 18.04**.

* Linux packages:

    `Docker engine (minimum version 18.06.0)`

    `Docker-compose (minimum version 1.23.2)`

* Internet connection

## Instructions

### Get dependent files

The following notations is used in this document:

* `<sdo-supply-chain-tools>`: supply-chain-tools
* `<sdo-supply-chain-tools-docker>`: \<sdo-supply-chain-tools>/demo/docker_manufacturer

Build the toolkit using the command:

```
$ cd <sdo-supply-chain-tools>
$ mvn package
```
This copies the manufacturer-webapp*.war, mt_create.sql and rt_create.sql into
\<sdo-supply-chain-tools-docker>. Following files should be present after build.

1. manufacturer-webapp*.war
2. mt_create.sql
3. rt_create.sql
4. mt_config.sql
5. rt_config.sql

### Default sample configurations

A docker-compose.yml with default configuration is present in the directory \<sdo-supply-chain-tools-docker>.
A sample keystore is provided at \<sdo-supply-chain-tools-docker>/keys/manufacturer-keystore.p12,
along with a set of default configurations at mt_config.sql and rt_config.sql.
This is an example implementation for demo purposes and should be updated in production deployment.
Modify mt_config.sql and rt_config.sql following instructions given in the respective files to customize for your environment,
and replace the sample keystore.

### OnDie ECDSA configuration

If OnDie ECDSA support is needed, run the script located at \<sdo-supply-chain-tools>/scripts/onDieCache.py,
to populate the directory \<sdo-supply-chain-tools-docker>/ondiecache with OnDie ECDSA certs and crls.
Refer to the README located at <sdo-supply-chain-tools> for more information.

### Create Java keystore file

See instructions in the Secure Device Onboard [Keystore Setup Guide](https://secure-device-onboard.github.io/docs/latest/supply-chain-tools/keystore-guide/). Once the file is created, update docker-compose.yml to reflect the file name, path and password. The default configured is \<sdo-supply-chain-tools-docker>/keys/manufacturer-keystore.p12.
The manufacturer will not start if the keystore is not present.

### Modify docker-compose.yml configuration as needed
Review the docker-compose.yml file and follow instructions in the file to customize for your environment.
Edit the application's properties stored in manufacturer-mariadb.env and manufacturer.env, as needed.

### Modify settings.xml configuration as needed
Edit settings.xml file and add your user and password.

### Modify tomcat-users.xml configuration as needed
Edit tomcat-users.xml file and add your admin user and password.

### Start/Stop Docker

* Use the following command to start the docker container.

```
$ cd <sdo-supply-chain-tools-docker>
$ sudo docker-compose up
```

* Use the following command to stop all running docker containers.

```
$ sudo docker stop $(sudo docker ps -a -q)
```

* Use the following command to delete all the docker artifacts. (Note: docker containers must be stopped before deleting
them)
```
$ sudo docker system prune -a
```

Your Docker container is now ready to support DI Protocol and voucher operations.  Additional information is available 
in the Secure Device Onboard Manufacturing Enablement Guide.