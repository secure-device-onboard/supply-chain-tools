USE `sdo` ;

#call mt_add_server_settings(rendezvous info, certificate_validity_period);
#rendezvous info = specifies the rendezvous server the device will contact during
#SDO TO protocol
#certificate_validity_period = period for ECC based device cert chain
#certificate_validity_period is specified according to ISO 8601 duration value
#duration format is: PnYnMnDTnHnMnS
#example duration: 30 days is specified as P30D
#
#Default values specified below: update as required 

call mt_add_server_settings(
	'http://sdo-sbx.trustedservices.intel.com:80',
	'P30D'
);

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

#call rt_add_customer_public_key(owner id, owner public key)
#owner id = specified the name/id of the owner.
#owner public key = specifies the public key of the owner to which the devices will
#be assigned to.
#sample owner with owner id as reseller_ecdsa_256 along with a sample ECDSA-256 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('reseller_ecdsa_256', '-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE305J06Z2xAqL4pwiKst8QXVEmJzO
lxgM43F4JSwI4XSKohIZ6GH6o1R25zrBgwXWE6imL754v/av1cHmwP8MSw==
-----END PUBLIC KEY-----
');

#sample owner with owner id as reseller_ecdsa_384 along with a sample ECDSA-384 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('reseller_ecdsa_384', '-----BEGIN PUBLIC KEY-----
MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEKJGKqlmZZyeFw/9fltM3QotdCFhQoj3w
plx5CFRmHdU3haPPKV8s0K+Fb2NO0gZXuF/bv5AUR5wL9/lDpQR9zgQgCNV2z6CZ
Mhs4RzFN34ss4Hx1uhakIVBem3ubtP2o
-----END PUBLIC KEY-----
');


#sample owner with owner id as reseller_rsa_2048 along with a sample RSA-2048 
#public key intended to be used for demo purposes.
#Replace the owner id and owner keys with your own for production deployment.
call rt_add_customer_public_key('reseller_rsa_2048', '-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyr0lXjWv0vdvzxTklqKk
sAD3Q0JEs8o12CQmybpn9QInC14SO4jRs592EVfun7CQGvxYk+1P4ll2ZctZa7QL
Dw3cmJnkAFHd5bpgVqgqq3oMmAqVQsZzgSoz5vlvINorPFvP8Qnif0QND5QaBRPA
OQEfZHJGCeAxPrU/6iVhZnZlTmoDJXBl8uUnM/suush8DQQkJSxQMG+A5goLdMgH
CpcrrnVFWIYPQZMlwtX+JVCvh+OpmFYukgqbc9RP68C99TI6X1206wUsS/wEsqA9
mKXCGo4hjbrFxaoFIEUFlOf3js7CIvotV0kNUaAIsF1Qz9dzKiEHXJCqaqksEd5z
FwIDAQAB
-----END PUBLIC KEY-----
');
