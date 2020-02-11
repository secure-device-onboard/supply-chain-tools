#!/bin/bash -e
#
# Copyright 2020 Intel Corporation
# SPDX-License-Identifier: Apache 2.0
#
#
# Generates a PKCS12 (p12) KeyStore for use with SDO.
#
# The following software is required:
#
# keytool
# https://openjdk.java.net/projects/jdk/11/
#
# openssl
# https://www.openssl.org/

CRT_VALID_DAYS=365
KEYSTORE_PW=123456

function mkeckey {
  openssl ecparam -name $1 -genkey -noout 2>/dev/null
}

function mkrsakey {
  openssl genrsa -F4 2048 2>/dev/null
}

function mkcert {
  openssl req -new -x509 -key - -out - -days ${CRT_VALID_DAYS} -batch 2>/dev/null
}

function mkp12 {
  openssl pkcs12 -export -name "$1" -password "$2"
}

WORKDIR=$(mktemp -p . -d tmp-XXX)

KEY="$(mkeckey prime256v1)"
CERT=$(echo "${KEY}" | mkcert)
echo -e "${KEY}\n${CERT}" | mkp12 prime256v1 "pass:${KEYSTORE_PW}" > $(mktemp -p "${WORKDIR}")

KEY="$(mkeckey secp384r1)"
CERT=$(echo "${KEY}" | mkcert)
echo -e "${KEY}\n${CERT}" | mkp12 secp384r1 "pass:${KEYSTORE_PW}" > $(mktemp -p "${WORKDIR}")

KEY="$(mkrsakey)"
CERT=$(echo "${KEY}" | mkcert)
echo -e "${KEY}\n${CERT}" | mkp12 rsa "pass:${KEYSTORE_PW}" > $(mktemp -p "${WORKDIR}")

PKCS12="${WORKDIR}/sdo.p12"

for P in "${WORKDIR}"/*; do
  keytool -importkeystore \
    -srckeystore "${P}" -srcstorepass "${KEYSTORE_PW}" \
    -destkeystore "${PKCS12}" -deststorepass "${KEYSTORE_PW}" \
    -noprompt 2>/dev/null
done

echo "${PKCS12}"
