-- Copyright 2020 Intel Corporation
-- SPDX-License-Identifier: Apache 2.0

USE `intel_sdo` ;

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

