// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.mt;

import java.security.KeyStore;
import java.security.cert.CertPath;
import java.util.Set;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

@FunctionalInterface
interface CertPathService {
  CertPath apply(final PKCS10CertificationRequest csr) throws Exception;
}
