-- Copyright 2020 Intel Corporation
-- SPDX-License-Identifier: Apache 2.0

USE `sdo` ;

#call mt_add_server_settings(rendezvous info, certificate_validity_period);
#rendezvous info = specifies the rendezvous server the device will contact during
#SDO TO protocol
#Multiple rendezvous entries can be specified by a space delimited list. For example:
#@rendezvous_info = 'http://sdo-sbx.trustedservices.intel.com:80 http://sdo-pbx.trustedservices.intel.com:80',
#certificate_validity_period = period for ECC based device cert chain
#certificate_validity_period is specified according to ISO 8601 duration value
#duration format is: PnYnMnDTnHnMnS
#example duration: 30 days is specified as P30D
#2 years is specified as P2Y
#
#Default values specified below: update as required 

call mt_add_server_settings(
	'http://sdo-sbx.trustedservices.intel.com:80',
	'P2Y'
);

