#!/bin/bash -e
#
# Copyright 2020 Intel Corporation
# SPDX-License-Identifier: Apache 2.0
#
# Generates a key and self-signed certificate and installs them into a YubiKey.
#
# The following software is required:
#
# YubiKey Manager
# https://www.yubico.com/products/services-software/download/yubikey-manager/
#
# openssl
# https://www.openssl.org/

function print_usage {
  cat << EOF
Usage: $0 KEY-TYPE

  Generate a key and self-signed certificate and install them into a YubiKey.

  KEY-TYPE  The type of key to generate.  Maybe be one of:
              ECCP256
              ECCP384
              RSA2048
EOF
}

function mkeckey {
  openssl ecparam -name $1 -genkey -noout -out $2
}

function mkrsakey {
  openssl genrsa -F4 -out $1 2048
}

if [ $# -ne 1 ]; then
  print_usage
  exit 1;
fi

WORKDIR=$(mktemp -p . -d tmp-XXX)

# generate the private key

KEY_TYPE=$(echo $1 | tr '[:upper:]' '[:lower:]')
KEY_FILE=${WORKDIR}/sdo-${KEY_TYPE}.key

case ${KEY_TYPE} in
  eccp256 | ecc256 | ec256 )
    mkeckey prime256v1 ${KEY_FILE}
    ;;
  eccp384 | ecc384 | ec384 )
    mkeckey secp384r1 ${KEY_FILE}
    ;;
  rsa2048 | rsa )
    mkrsakey ${KEY_FILE}
    ;;
  * )
    print_usage
    exit 1
esac

CRT_VALID_DAYS=365

CRT_FILE=${WORKDIR}/sdo-${KEY_TYPE}.crt

# generate a self-signed certificate for the key

openssl req -new -x509 -key ${KEY_FILE} -out ${CRT_FILE} -days ${CRT_VALID_DAYS} -batch

# import the key and certificate into PIV slot 9a of the YubiKey

YK_MKEY=010203040506070801020304050607080102030405060708
yes | ykman piv reset
ykman piv import-key --management-key ${YK_MKEY} 9a ${KEY_FILE}
ykman piv import-certificate --management-key ${YK_MKEY} 9a ${CRT_FILE}

echo ${KEY_FILE}
echo ${CRT_FILE}
