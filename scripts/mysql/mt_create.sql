-- Copyright 2020 Intel Corporation
-- SPDX-License-Identifier: Apache 2.0

-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema sdo
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema sdo
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `sdo` DEFAULT CHARACTER SET utf8 ;
USE `sdo` ;

-- -----------------------------------------------------
-- Table `sdo`.`mt_device_state`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sdo`.`mt_device_state` ;

CREATE TABLE IF NOT EXISTS `sdo`.`mt_device_state` (
  `device_serial_no` VARCHAR(128) NOT NULL,
  `di_start_datetime` DATETIME NOT NULL,
  `di_end_datetime` DATETIME NULL,
  `session_data` LONGTEXT NULL,
  `status` INT NULL COMMENT 'In-process = 0, pass = 1, fail = -1, or timeout = -2.',
  `details` VARCHAR(2048) NULL,
  PRIMARY KEY (`device_serial_no`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `sdo`.`mt_server_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sdo`.`mt_server_settings` ;

CREATE TABLE IF NOT EXISTS `sdo`.`mt_server_settings` (
  `id` INT NOT NULL,
  `rendezvous_info` LONGTEXT NULL,
  `certificate_validity_period` VARCHAR(128) NULL COMMENT 'ISO 8601 format',
  PRIMARY KEY (`id`))
ENGINE = InnoDB;

USE `sdo` ;

-- -----------------------------------------------------
-- Placeholder table for view `sdo`.`v_mt_device_state`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sdo`.`v_mt_device_state` (`di_start_datetime` INT, `di_end_datetime` INT, `device_serial_no` INT, `details` INT, `Col_placeholder1` INT);

-- -----------------------------------------------------
-- Placeholder table for view `sdo`.`v_mt_version`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sdo`.`v_mt_version` (`0` INT);

-- -----------------------------------------------------
-- Placeholder table for view `sdo`.`v_mt_server_settings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `sdo`.`v_mt_server_settings` (`rendezvous_info` INT, `certificate_validity_period` INT);

-- -----------------------------------------------------
-- procedure mt_timeout_device_sessions
-- -----------------------------------------------------

USE `sdo`;
DROP procedure IF EXISTS `sdo`.`mt_timeout_device_sessions`;

DELIMITER $$
USE `sdo`$$
create procedure mt_timeout_device_sessions(IN timeout_minutes int, out count int) 
BEGIN
   update device_state set status = -2 where timestampdiff(MINUTE, current_date(), di_start_datetime) > timeout_minutes;
   set count = (select row_count());
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure mt_purge_device_state
-- -----------------------------------------------------

USE `sdo`;
DROP procedure IF EXISTS `sdo`.`mt_purge_device_state`;

DELIMITER $$
USE `sdo`$$
CREATE PROCEDURE mt_purge_device_state (in older_than_minutes int, out count int)
BEGIN
   delete from device_state where timestampdiff(MINUTE, current_date(), di_end_datetime) > older_than_minutes;
   set count = (select row_count());
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure mt_add_server_settings
-- -----------------------------------------------------

USE `sdo`;
DROP procedure IF EXISTS `sdo`.`mt_add_server_settings`;

DELIMITER $$
USE `sdo`$$
create procedure mt_add_server_settings
(
    rendezvous_info mediumtext,
    certificate_validity_period varchar(45)
)
BEGIN
   replace into mt_server_settings (id, rendezvous_info, certificate_validity_period)
		values (1, rendezvous_info, certificate_validity_period);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- View `sdo`.`v_mt_device_state`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sdo`.`v_mt_device_state`;
DROP VIEW IF EXISTS `sdo`.`v_mt_device_state` ;
USE `sdo`;
CREATE  OR REPLACE VIEW `v_mt_device_state` AS
select 
  ds.di_start_datetime, ds.di_end_datetime, 
  ds.device_serial_no,
  ds.details,
  case when ds.status = 0 then 'IN-PROCESS' 
	when ds.status = 1 then 'SUCCESS'
	when ds.status = -1 then 'FAIL'
	when ds.status = -2 then 'TIMED OUT'
  else 'Unknown' end AS 'Col_placeholder1' 
from mt_device_state ds;

-- -----------------------------------------------------
-- View `sdo`.`v_mt_version`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sdo`.`v_mt_version`;
DROP VIEW IF EXISTS `sdo`.`v_mt_version` ;
USE `sdo`;
CREATE  OR REPLACE VIEW `v_mt_version` AS
# this simply returns the current version of the mt database
	select 0;

-- -----------------------------------------------------
-- View `sdo`.`v_mt_server_settings`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sdo`.`v_mt_server_settings`;
DROP VIEW IF EXISTS `sdo`.`v_mt_server_settings` ;
USE `sdo`;
CREATE  OR REPLACE VIEW `v_mt_server_settings` AS
	select rendezvous_info, certificate_validity_period
    from mt_server_settings;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
