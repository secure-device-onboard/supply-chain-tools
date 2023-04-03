***Note:***  This product has been discontinued. We recommend users to switch to FDO components (https://github.com/secure-device-onboard/pri-fidoiot).

# Secure Device Onboard (SDO) Supply Chain Tools Quick Start

The SDO Supply Chain Tools include multiple web archives (.war files).
Which one you select will depend upon your task:

- Generating new vouchers by initializing devices with the SDO device initialization (DI) protocol
and assigning those vouchers to another party is referred to in this README file
as the role of *manufacturer*.
Manufacturers use the <code>manufacturer-webapp</code> WAR.

- Accepting existing vouchers and re-assigning them to another party without
accessing the device is referred to in this README file as the role of *reseller*.
Resellers use the <code>reseller-webapp WAR</code>.

## Building the Toolkits

To build the toolkits go to the supply-chain-tools directory and run:
mvn install

***Note:*** Towards the end of the build you may notice a couple of error messages. These occur
due to cleanup of unit test artifacts and do not indicate any issue with the build itself.
You can safely ignore these messages.

## Required Software

You will need some supporting software installed to use the SDO
Toolkits.

### Java* Servlet Container

This README file refers to the system running the servlet container as the
*web server*.

The SDO Supply Chain web applications must be installed in a Java
servlet container like [Apache Tomcat*][1].

For testing, the WAR files are also executable via the [Spring Boot* platform][7].

The SDO Supply Chain tools are configured via Java properties.
In this README file, there are some properties which need to be updated based on servlet container
mechanism. Your choice of servlet container will dictate how you set these properties.
See your servlet container's documentation for specific instructions.

Common mechanisms include the following:

- [Apache Tomcat software's][1] [<code>catalina.properties</code>][6].

- [Spring Boot software's][7] [<code>application.properties</code>][8].

### SQL Database

This README file refers to the system running the database as the
*database server*.

The SDO Toolkits use an SQL database to provide access control,
system configuration, and data persistence. The database is not intended
for long-term storage of SDO artifacts. It is strongly encouraged to make
external backups of generated SDO vouchers.

The SDO Toolkits were tested with [MariaDB* server][9].

Two scripts are provided to initialize the database:
[mt_create.sql](scripts/mysql/mt_create.sql)
and
[rt_create.sql](scripts/mysql/rt_create.sql).

If you will be acting as a *manufacturer*, run both scripts to initialize
your database.

If you will be acting as a *reseller*, run the <code>rt_create.sql</code>
script to initialize your database.

These scripts will install a schema named <code>sdo</code> which you
must include in your JDBC* connection string, below.

The SDO Supply Chain Tool Enablement Guides contain details about
setting up your database and database users.  To proceed, you'll need
these things:

- Your database [JDBC connection string][10].
- The name of the database account that the SDO Toolkit software will use.
- The password of that database account.

Place this data in the [Spring Data Java properties][2] to connect the
web application to the database.  For example:

    spring.datasource.url=jdbc:mariadb://database.example.com:3306/sdo
    spring.datasource.username=sdo_db_user
    spring.datasource.password=sdo_db_password

### Public Key Cryptography Standards (PKCS#11) Smart Card Library

If you will be using PKCS#11 hardware keystores, install a PKCS#11 smart card
library on your web server.

The SDO Supply Chain Tools were tested with the [OpenSC library][3].

## Generating Keystores

SDO toolkits use the keys
in Java keystores to generate cyptographic signatures.
You'll need at least one.  Hardware keystores are encouraged,
but software keystores are also supported.

### If you're using PKCS#11 Hardware Keys:

The SDO Toolkits were tested with [YubiKey* 5 security keys][4].

Each SDO key algorithm (ECCP256, ECCP384, RSA2048) will need its own
hardware key. For each algorithm you want to enable, you will need
one hardware key.  If you do not support a particular algorithm, you do not need
to create a key for that algorithm.

If you are using YubiKey 5 keys with the SDO Toolkits, the
[ykinit.sh](scripts/ykinit.sh) bash script can be used to initialize each key.

To use ykinit.sh, insert only one YubiKey security key into the computer and run
ykinit.sh.  The script requires one command-line argument: the algorithm
of the key to generate.  For example:

    ykinit.sh ECCP384

As it runs, the script will generate Privacy-Enhanced Mail (PEM)-formatted key and certificate
backup files.  The names of these files will be printed to the console.
Make sure to back these files up to a secure location in case you need
to recreate the key in future.

***Note:*** The ykinit script will reformat your YubiKey security key before it stores the new key.
Don't use this script on YubiKey security keys that contain important data.
It is strongly suggested that you use the [YubiKey Manager tool][5]
to change the PIN on your YubiKey security key after it has been initialized.

After your YubiKey security keys have been initialized, plug them into your web server.

Set these Java properties in your servlet container to enable auto-detection
of the keys when the servlet container starts:

- <code>sdo.keystore</code>

Set this to the file: Uniform Resource Identifier (URI) of your installed [PKCS#11 provider library][3].

Example:

    sdo.keystore=file:///C:/Program Files/OpenSC Project/OpenSC/pkcs11/opensc-pkcs11.dll

- <code>sdo.keystore.password</code>

Set this to the PIN or password of your PKCS#11 keystores.

Example:

    sdo.keystore.password=changeit

### If you're using a PKCS#12 Software Keystore:

A PKCS#12 software keystore may be used instead of PKCS#11 hardware keys.

The [p12init.sh](scripts/p12init.sh) bash script can be used to generate
a suitable PKCS#12 keystore.  The script will print the filename of the
generated keystore to the console. It is strongly suggested that you back
this file up in a secure location.

It is also strongly suggested that you edit this script before use and customize
the variables at the top to suit your needs:

<code>CRT_VALID_DAYS</code>: the validity period, in days, of the certificates
in the keystore.

<code>KEYSTORE_PW</code>: the created keystore's password.

Set these Java properties in your servlet container:

- <code>sdo.keystore</code>

Set this to the file: URI of your PKCS#12 keystore file.

Example:

    sdo.keystore=file:///usr/local/sdo/key.p12

- <code>sdo.keystore.password</code>

Set this to the PIN or password of your PKCS#12 keystore.

Example:

    sdo.keystore.password=changeit

## REST API

The SDO Toolkits provide the following REpresentational State Transfer (REST) API:

### GET api/version

Returns the web application's version.

Example:

    $ curl -G http://sdo.example.com/api/version

    {
        "build":"5e74afb6e1e1ff5ec88dc9350dfebeaf977c30be",
        "version":"1.6.0",
        "timestamp":"2019-08-08T20:53:49Z"
    }

### GET api/v1/status

Returns the web application's database connection status.

Example:

    $ curl -G http://sdo.example.com/api/v1/status

    {
        "connection.databaseProductName":"MySQL",
        "connection.databaseProductVersion":"10.4.7-MariaDB",
        "connection.url":"jdbc:mariadb://sdodb.example.com:3306/sdo",
        "connection.user":"sdo"
    }

### GET api/v1/vouchers/*device-serial-number*

    $ curl -G http://sdo.example.com/api/v1/vouchers/01020304

    {"sz":1,"oh":{"pv":112,"pe":1,"r":[2,[4,{"dn"...

Returns the SDO voucher for the identified device.

## Running the Toolkits

The SDO Supply Chain Toolkits come with stored procedures which you'll
use to control the toolkit. See the Enablement Guide
for your toolkit for detailed information.

### Running the Manufacturer Toolkit

To use the manufacturer toolkit:

- Before you start the servlet container, run the stored procedure
<code>mt_add_server_settings</code> to configure the SDO engine.
If you changed the data in this table, restart the servlet container.

- Start the web server.

- Use the web server to initialize SDO-enabled devices via the SDO DI protocol.

- Run the stored procedure <code>rt_add_customer_public_key</code> to install
public keys for entities that will receive your generated SDO vouchers.

- Run the stored procedure <code>rt_assign_device_to_customer</code> to assign
generated vouchers to the entities you identified in the previous step.

- Use the toolkit REST API to download the generated vouchers.

### Running the Reseller Toolkit

To use the reseller toolkit:

- Start the web server.

- Run the stored procedure <code>rt_add_voucher</code> to place received vouchers
in the database.

- Run the stored procedure <code>rt_add_customer_public_key</code> to install
public keys for entities that will receive your generated SDO vouchers.

- Run the stored procedure <code>rt_assign_device_to_customer</code> to assign
generated vouchers to the entities you identified in the previous step.

- Use the toolkit REST API to download the generated vouchers.

## Configuring OnDie ECDSA

If supporting OnDie ECDSA devices, set the following Java properties in your servlet container. In typical
environments, only sdo.ondiecache.cachedir is used. The cachedir is used to store CRL and cert files and
can be populated by running the provided script (scripts/onDieCache.py) to load the directory with the files
from the cloud.

To run the onDieCache.py script, python3 must be installed.
Invoke as follows: python3 onDieCache.py CACHEDIR
where CACHEDIR = target location to store CRL and cert files. Typically, this will match the value of sdo.ondiecache.cachedir.
However, it is also possible to run the script on a different host and then copy the downloaded files into sdo.ondiecache.cachedir.
The exact setup will vary depending on the runtime environment and user needs.

- <code>sdo.ondiecache.cachedir</code>

Set this to the file: Uniform Resource Identifier (URI) of the directory that contains the cert and CRL files.

Example:

    sdo.ondiecache.cachedir=file:///C:/SDO/ondiecache


- <code>sdo.ondiecache.revocations</code>

Set this to true or false to enable revocation checking of OnDie ECDSA signatures. The default value is
false. This should not be set to true in production environments and should only set to false when debugging
or working with pre-production hardware.

Example:

    sdo.ondiecache.revocations=true


[1]: http://tomcat.apache.org
[2]: https://spring.io/guides/gs/accessing-data-mysql/#_create_the_application_properties_file
[3]: https://github.com/OpenSC/OpenSC/wiki
[4]: https://www.yubico.com/products/yubikey-5-overview/
[5]: https://www.yubico.com/products/services-software/download/yubikey-manager/
[6]: https://tomcat.apache.org/tomcat-9.0-doc/config/index.html
[7]: https://spring.io/projects/spring-boot
[8]: https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html
[9]: https://mariadb.org/
[10]: https://mariadb.com/kb/en/library/about-mariadb-connector-j/#connection-strings
