--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

USE [sdo]
GO

IF EXISTS 
    (SELECT name  
     FROM master.dbo.syslogins 
		where name = 'sdo' and dbname = 'sdo')
BEGIN
	DROP USER sdo;
END
GO

IF EXISTS 
    (SELECT name  
     FROM master.sys.server_principals
     WHERE name = 'sdo')
BEGIN
	DROP LOGIN sdo;
END
GO 

CREATE LOGIN sdo WITH PASSWORD = 'sdo', CHECK_POLICY = OFF, DEFAULT_DATABASE = [sdo];
GO

USE [sdo]
GO

CREATE USER sdo FOR LOGIN sdo;
GO

GRANT EXEC, DELETE, INSERT, UPDATE, SELECT to sdo;
GO

