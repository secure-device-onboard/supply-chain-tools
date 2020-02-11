--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

#Run this script with the "root" mariadb user.

#The password for each of the users should be a hash of the password.
#To create the hash value, run "SELECT PASSWORD('sdo');" against the database.
#Replace 'sdo' with the actual password you wish to use.
#NOTE: be sure to provide different passwords for each user!
#The reason for using hash values in this script is to prevent leaving passwords exposed.

#sdo_admin = 
#	user given to the Toolkit web services for database access and for anyone else needing rw access
#	such as the business systems for the reseller and/or manufacturer
#sdo_user =
#	for any user needing only read only access
#	this user can only read data and cannot run any stored procedure

USE `intel_sdo` ;

CREATE USER IF NOT EXISTS sdo_admin IDENTIFIED BY PASSWORD '*C1365FA6C0ECF1E0BAAA4039DF59D21A3838700E';
CREATE USER IF NOT EXISTS sdo_user IDENTIFIED BY PASSWORD '*C1365FA6C0ECF1E0BAAA4039DF59D21A3838700E';

GRANT ALL PRIVILEGES ON intel_sdo.* To 'sdo_admin'@'%';
GRANT SELECT ON intel_sdo.* To 'sdo_user'@'%';


