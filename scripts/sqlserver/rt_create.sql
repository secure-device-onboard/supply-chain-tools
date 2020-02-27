--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

IF NOT EXISTS ( SELECT  *
                FROM    sys.sysdatabases
                WHERE   name = N'intel_sdo' )
    EXEC('CREATE DATABASE [intel_sdo]');
GO

USE [intel_sdo]
GO

SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


/***** TABLES *********************************/

DROP TABLE IF EXISTS [dbo].[rt_ownership_voucher];
GO
DROP TABLE IF EXISTS [dbo].[rt_customer_public_key];
GO

CREATE TABLE [dbo].[rt_customer_public_key](
	[customer_public_key_id] [int] IDENTITY(1,1) NOT NULL,
	[customer_descriptor] [varchar](64) NOT NULL,
	[public_key_pem] [text] NOT NULL,
	PRIMARY KEY (customer_public_key_id)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE UNIQUE INDEX customer_descriptor_UNIQUE ON [dbo].[rt_customer_public_key] (customer_descriptor ASC)
GO

ALTER TABLE [dbo].[rt_customer_public_key] ADD  DEFAULT ('') FOR [customer_descriptor]
GO


CREATE TABLE [dbo].[rt_ownership_voucher](
	[device_serial_no] [varchar](128) NOT NULL,
	[voucher] [text] NOT NULL,
	[customer_public_key_id] [int] FOREIGN KEY REFERENCES [dbo].[rt_customer_public_key] (customer_public_key_id),
	[uuid] [varchar](64) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO


/***** VIEWS *********************************/

DROP VIEW IF EXISTS [dbo].[v_rt_ownership_voucher];
GO

CREATE VIEW [dbo].[v_rt_ownership_voucher]
AS SELECT [device_serial_no], [voucher] FROM rt_ownership_voucher;
GO

DROP VIEW IF EXISTS [dbo].[v_rt_customer_public_key];
GO

CREATE VIEW [dbo].[v_rt_customer_public_key]
AS SELECT [customer_descriptor], [public_key_pem] FROM rt_customer_public_key;
GO


/***** PROCEDURES *********************************/

DROP procedure IF EXISTS [dbo].[rt_add_voucher];
GO

create procedure [dbo].[rt_add_voucher] @device_serial_no varchar(128), @voucher text
AS
	IF NOT EXISTS (select * from rt_ownership_voucher where device_serial_no = @device_serial_no)
		insert into rt_ownership_voucher(device_serial_no, voucher, customer_public_key_id)
		values (@device_serial_no, @voucher, null);
	ELSE
		update rt_ownership_voucher SET voucher = @voucher where device_serial_no = @device_serial_no;
GO


DROP procedure IF EXISTS [dbo].[rt_add_customer_public_key];
GO

create procedure [dbo].[rt_add_customer_public_key] 
    @customer_descriptor varchar(64), 
    @public_key_pem text
AS
    IF NOT EXISTS (select * from rt_customer_public_key where customer_descriptor = @customer_descriptor)
		insert rt_customer_public_key (customer_descriptor, public_key_pem)
			values (@customer_descriptor, @public_key_pem);
	ELSE
		update rt_customer_public_key SET public_key_pem = @public_key_pem where customer_descriptor = @customer_descriptor;
GO


DROP procedure IF EXISTS [dbo].[rt_assign_device_to_customer];
GO

CREATE PROCEDURE rt_assign_device_to_customer @device_serial_number varchar(128), @customer_desc varchar(64)
AS
	update [dbo].[rt_ownership_voucher] set customer_public_key_id = 
		(select customer_public_key_id 
        from rt_customer_public_key 
        where customer_descriptor = @customer_desc)
	where device_serial_no = @device_serial_number;
	
	if @@ROWCOUNT = 0
      THROW 50000, 'Customer not found', 1;
GO



