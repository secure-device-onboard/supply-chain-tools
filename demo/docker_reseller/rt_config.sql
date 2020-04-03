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

USE `sdo` ;

CREATE USER IF NOT EXISTS sdo_admin IDENTIFIED BY PASSWORD '*C1365FA6C0ECF1E0BAAA4039DF59D21A3838700E';
CREATE USER IF NOT EXISTS sdo_user IDENTIFIED BY PASSWORD '*C1365FA6C0ECF1E0BAAA4039DF59D21A3838700E';

GRANT ALL PRIVILEGES ON sdo.* To 'sdo_admin'@'%';
GRANT SELECT ON sdo.* To 'sdo_user'@'%';


#call rt_add_customer_public_key(owner id, owner public key)
#owner id = specified the name/id of the owner.
#owner public key = specifies the public key of the owner to which the devices will
#be assigned to.
#sample owner with owner id as owner_ecdsa_256 along with a sample ECDSA-256 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('owner_ecdsa_256', '-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWVUE2G0GLy8scmAOyQyhcBiF/fSU
d3i/Og7XDShiJb2IsbCZSRqt1ek15IbeCI5z7BHea2GZGgaK63cyD15gNA==
-----END PUBLIC KEY-----
');

#sample owner with owner id as owner_ecdsa_384 along with a sample ECDSA-384 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('owner_ecdsa_384', '-----BEGIN PUBLIC KEY-----
MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE4RFfGVQdojLIODXnUT6NqB6KpmmPV2Rl
aVWXzdDef83f/JT+/XLPcpAZVoS++pwZpDoCkRU+E2FqKFdKDDD4g7obfqWd87z1
EtjdVaI1qiagqaSlkul2oQPBAujpIaHZ
-----END PUBLIC KEY-----
');

#sample owner with owner id as owner_rsa_2048 along with a sample RSA-2048 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('owner_rsa_2048', '-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtE58Wx9S4BWTNdrTmj3+
kJXNKuOAk3sgQwvF0Y8uXo3/ECeS/hj5SDmxG5fSnBlmGVKJwGV1bTVERDZ4uh4a
W1fWMmoUd4xcxun4N4B9+WDSQlX/+Rd3wBLEkKQfNr7lU9ZitfaGkBKxs23Y0GCY
Hfwh91TjXzNtGzAzv4F/SqQ45KrSafQIIEj72yuadBrQuN+XHkagpJwFtLYr0rbt
RZfSLcSvoGZtpwW9JfIDntC+eqoqcwOrMRWZAnyAY52GFZqK9+cjJlXuoAS4uH+q
6KHgLC5u0rcpLiDYJgiv56s4pwd4ILSuRGSohCYsIIIk9rD+tVWqFsGZGDcZXU0z
CQIDAQAB
-----END PUBLIC KEY-----
');
