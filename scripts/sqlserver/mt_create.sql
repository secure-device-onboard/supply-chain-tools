--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

IF NOT EXISTS ( SELECT  *
                FROM    sys.sysdatabases
                WHERE   name = N'sdo' )
    EXEC('CREATE DATABASE [sdo]');
GO

USE [sdo]
GO


/***** TABLES *********************************/

DROP TABLE IF EXISTS [dbo].[mt_device_state];
GO

DROP TABLE IF EXISTS [dbo].[mt_device_state];
GO

CREATE TABLE [dbo].[mt_device_state] (
  [device_serial_no] VARCHAR(128) NOT NULL,
  [di_start_datetime] DATETIME NOT NULL,
  [di_end_datetime] DATETIME NULL,
  [session_data] TEXT NULL,
  [status] INT NULL,
  [details] VARCHAR(2048) NULL,
  PRIMARY KEY (device_serial_no));
GO


DROP TABLE IF EXISTS [dbo].[mt_server_settings];
GO

CREATE TABLE [dbo].[mt_server_settings] (
  [id] INT NOT NULL,
  [rendezvous_info] TEXT NULL,
  [certificate_validity_period] VARCHAR(128),
  PRIMARY KEY (id))
GO

/***** VIEWS *********************************/

DROP VIEW IF EXISTS [dbo].[v_mt_device_state];
GO

CREATE VIEW [dbo].[v_mt_device_state]
AS SELECT ds.di_start_datetime, ds.di_end_datetime, 
  ds.device_serial_no,
  ds.details,
  case when ds.status = 0 then 'IN-PROCESS' 
	when ds.status = 1 then 'SUCCESS'
	when ds.status = -1 then 'FAIL'
	when ds.status = -2 then 'TIMED OUT'
  else 'Unknown' end AS 'Col_placeholder1' 
from mt_device_state ds;
GO

DROP VIEW IF EXISTS [dbo].[v_mt_server_settings];
GO

CREATE VIEW [dbo].[v_mt_server_settings]
AS
	SELECT rendezvous_info, certificate_validity_period
    from mt_server_settings;
GO

/***** PROCEDURES *********************************/

DROP procedure IF EXISTS [dbo].[mt_timeout_device_sessions];
GO

CREATE PROCEDURE [dbo].[mt_timeout_device_sessions] @timeout_minutes int, @count int
AS
   update [dbo].[mt_device_state] set status = -2 where datediff(MINUTE, CURRENT_TIMESTAMP, di_start_datetime) > @timeout_minutes;
   set @count = @@ROWCOUNT;
GO

DROP procedure IF EXISTS [dbo].[mt_purge_device_state];
GO

CREATE PROCEDURE [dbo].[mt_purge_device_state] @older_than_minutes int, @count int
AS
   delete from [dbo].[mt_device_state] where datediff(MINUTE, CURRENT_TIMESTAMP, di_end_datetime) > @older_than_minutes;
   set @count = @@ROWCOUNT;
GO

DROP procedure IF EXISTS [dbo].[mt_add_server_settings];
GO

create procedure [dbo].[mt_add_server_settings]
	@rendezvous_info text,
    @certificate_validity_period varchar(45)
AS
	IF NOT EXISTS (SELECT * FROM [dbo].[mt_server_settings] WHERE id = 1)
		insert into mt_server_settings (id, rendezvous_info, certificate_validity_period)
		values (1, @rendezvous_info, @certificate_validity_period);
	ELSE
		update [dbo].[mt_server_settings] 
			SET rendezvous_info = @rendezvous_info, 
				certificate_validity_period = @certificate_validity_period;
GO



