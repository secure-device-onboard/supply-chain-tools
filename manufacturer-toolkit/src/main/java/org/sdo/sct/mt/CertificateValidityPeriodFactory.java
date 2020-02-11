// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0


package org.sdo.sct.mt;

import java.time.Period;

@FunctionalInterface
interface CertificateValidityPeriodFactory {
  Period get();
}
