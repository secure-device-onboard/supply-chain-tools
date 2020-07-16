--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

USE sdo;
GO

--call mt_add_server_settings(rendezvous info, certificate_validity_period);
--rendezvous info = specifies the rendezvous server the device will contact during
--SDO TO protocol (initial boot at end customer site)
--Multiple rendezvous entries can be specified by a space delimited list. For example:
--@rendezvous_info = 'http://sdo-sbx.trustedservices.intel.com:80 http://sdo-pbx.trustedservices.intel.com:80',
--certificate_validity_period = period for ECC based device cert chain
--certificate_validity_period is specified according to ISO 8601 duration value
--duration format is: PnYnMnDTnHnMnS
--example duration: 30 days is specified as P30D
--
--Default: update as needed for your environment

exec [dbo].mt_add_server_settings 
	@rendezvous_info = 'http://sdo-sbx.trustedservices.intel.com:80',
    @certificate_validity_period = 'P30D';
GO

