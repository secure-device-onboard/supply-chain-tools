--  Copyright 2020 Intel Corporation
--  SPDX-License-Identifier: Apache 2.0

-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema intel_sdo
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema intel_sdo
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `intel_sdo` ;
USE `intel_sdo` ;

-- -----------------------------------------------------
-- Table `intel_sdo`.`rt_customer_public_key`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `intel_sdo`.`rt_customer_public_key` ;

CREATE TABLE IF NOT EXISTS `intel_sdo`.`rt_customer_public_key` (
  `customer_public_key_id` INT NOT NULL AUTO_INCREMENT,
  `customer_descriptor` VARCHAR(64) NOT NULL DEFAULT '',
  `public_key_pem` LONGTEXT NULL,
  PRIMARY KEY (`customer_public_key_id`),
  UNIQUE INDEX `customer_descriptor_UNIQUE` (`customer_descriptor` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `intel_sdo`.`rt_ownership_voucher`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `intel_sdo`.`rt_ownership_voucher` ;

CREATE TABLE IF NOT EXISTS `intel_sdo`.`rt_ownership_voucher` (
  `device_serial_no` VARCHAR(128) NOT NULL,
  `voucher` LONGTEXT NOT NULL,
  `customer_public_key_id` INT NULL,
  `uuid` VARCHAR(64) NULL,
  PRIMARY KEY (`device_serial_no`),
  INDEX `fk_certificate_id_idx` (`customer_public_key_id` ASC) ,
  CONSTRAINT `fk_public_key_id`
    FOREIGN KEY (`customer_public_key_id`)
    REFERENCES `intel_sdo`.`rt_customer_public_key` (`customer_public_key_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

USE `intel_sdo` ;

-- -----------------------------------------------------
-- Placeholder table for view `intel_sdo`.`v_rt_ownership_voucher`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intel_sdo`.`v_rt_ownership_voucher` (`device_serial_no` INT, `voucher` INT);

-- -----------------------------------------------------
-- Placeholder table for view `intel_sdo`.`v_rt_version`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intel_sdo`.`v_rt_version` (`0` INT);

-- -----------------------------------------------------
-- Placeholder table for view `intel_sdo`.`v_rt_customer_public_key`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intel_sdo`.`v_rt_customer_public_key` (`customer_descriptor` INT, `public_key_pem` INT);

-- -----------------------------------------------------
-- procedure rt_add_voucher
-- -----------------------------------------------------

USE `intel_sdo`;
DROP procedure IF EXISTS `intel_sdo`.`rt_add_voucher`;

DELIMITER $$
USE `intel_sdo`$$
create procedure rt_add_voucher(in device_serial_no varchar(128), in voucher longtext)
BEGIN
	replace into rt_ownership_voucher(device_serial_no, voucher, customer_public_key_id)
    values (device_serial_no, voucher, null);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure rt_add_customer_public_key
-- -----------------------------------------------------

USE `intel_sdo`;
DROP procedure IF EXISTS `intel_sdo`.`rt_add_customer_public_key`;

DELIMITER $$
USE `intel_sdo`$$
create procedure rt_add_customer_public_key (
	in customer_descriptor varchar(64), 
    in public_key_pem longtext
    )
BEGIN
	replace into rt_customer_public_key (customer_descriptor, public_key_pem)
		values (customer_descriptor, public_key_pem);
END$$

DELIMITER ;

-- -----------------------------------------------------
-- procedure rt_assign_device_to_customer
-- -----------------------------------------------------

USE `intel_sdo`;
DROP procedure IF EXISTS `intel_sdo`.`rt_assign_device_to_customer`;

DELIMITER $$
USE `intel_sdo`$$
create procedure rt_assign_device_to_customer (device_serial_number varchar(128), customer_desc varchar(64))
BEGIN
	update rt_ownership_voucher set customer_public_key_id = 
		(select customer_public_key_id 
        from rt_customer_public_key 
        where customer_descriptor = customer_desc)
	where device_serial_no = device_serial_number;
    # signal error if customer not found
    # (45000 is the mysql generic user error)
    set @count = (select row_count());
    if @count = 0 then
	  SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Customer not found';
      end if;
END$$

DELIMITER ;

-- -----------------------------------------------------
-- View `intel_sdo`.`v_rt_ownership_voucher`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `intel_sdo`.`v_rt_ownership_voucher`;
DROP VIEW IF EXISTS `intel_sdo`.`v_rt_ownership_voucher` ;
USE `intel_sdo`;
CREATE  OR REPLACE VIEW `v_rt_ownership_voucher` AS
    SELECT 
        ov.device_serial_no,
        ov.voucher
    FROM
        rt_ownership_voucher AS ov;

-- -----------------------------------------------------
-- View `intel_sdo`.`v_rt_version`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `intel_sdo`.`v_rt_version`;
DROP VIEW IF EXISTS `intel_sdo`.`v_rt_version` ;
USE `intel_sdo`;
CREATE  OR REPLACE VIEW `v_rt_version` AS
# this simply returns the current version of the rt database
	select 0;

-- -----------------------------------------------------
-- View `intel_sdo`.`v_rt_customer_public_key`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `intel_sdo`.`v_rt_customer_public_key`;
DROP VIEW IF EXISTS `intel_sdo`.`v_rt_customer_public_key` ;
USE `intel_sdo`;
CREATE  OR REPLACE VIEW `v_rt_customer_public_key` AS
	select 
        customer_descriptor,
        public_key_pem
	from rt_customer_public_key;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
