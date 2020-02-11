// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.sdo.sct.ResourceBundleHolder;
import org.slf4j.LoggerFactory;

/**
 * The JPA Entity representing the state of DI sessions.
 *
 * <p>These entities record the progress of SDO Devices as they complete DI.
 */
@Entity
@Table(name = "mt_device_state")
public class DeviceState {

  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(DeviceState.class.getName());

  /**
   * device_serial_no.
   *
   * @see DeviceState#getDeviceSerialNo()
   */
  @Id
  @Column(name = "device_serial_no", length = Limits.SERIAL_NUMBER_MAXLEN, nullable = false)
  private String deviceSerialNo;

  /**
   * di_start_datetime.
   *
   * @see DeviceState#getDiStart()
   */
  @Column(name = "di_start_datetime", nullable = false)
  private Timestamp diStart;

  /**
   * di_end_datetime.
   *
   * @see DeviceState#getDiEnd()
   */
  @Column(name = "di_end_datetime")
  private Timestamp diEnd;

  /**
   * session_data.
   *
   * @see DeviceState#getSessionData()
   */
  @Column(name = "session_data")
  @Lob
  private String sessionData;

  /**
   * status.
   *
   * @see DeviceState#getStatus()
   */
  @Column(name = "status")
  private Integer status;

  /**
   * details.
   *
   * @see DeviceState#getDetails()
   */
  @Column(name = "details", length = Limits.DETAILS_MAXLEN)
  private String details;

  public DeviceState() {
  }

  /**
   * device state.
   *
   * @param deviceSerialNo deviceSerialNo
   * @param diStart diStart
   * @param diEnd diEnd
   * @param sessionData sessionData
   * @param status status
   * @param details details
   */
  public DeviceState(
      final String deviceSerialNo,
      final Timestamp diStart,
      final Timestamp diEnd,
      final String sessionData,
      final Integer status,
      final String details) {

    setDeviceSerialNo(deviceSerialNo);
    setDiStart(diStart);
    setDiEnd(diEnd);
    setSessionData(sessionData);
    setStatus(status);
    setDetails(details);
  }

  /**
   * A Human-friendly description of the session status, or the reason for it, if available.
   *
   * <p>This field may be NULL if no extra information is available.
   */
  public String getDetails() {
    return details;
  }

  /**
   * set details.
   *
   * @see DeviceState#getDetails()
   */
  public void setDetails(String details) {
    if (null != details && details.length() > Limits.DETAILS_MAXLEN) {
      LoggerFactory.getLogger(getClass())
        .warn(resourceBundleHolder_.get().getString("details.too.long"));
      details = details.substring(0, Limits.DETAILS_MAXLEN);
    }
    this.details = details;
  }

  /**
   * The device's unique serial number (or equivalent).
   *
   * <p>This should not be the same as SDO's PM.OwnershipVoucher.oh.g.
   * 'g' is a transaction identifier and is different after every SDO transfer/resale.
   * If the device is transferred more than once, using g as device serial will cause collisions.
   */
  public String getDeviceSerialNo() {
    return deviceSerialNo;
  }

  /**
   * set device serial no.
   *
   * @see DeviceState#getDeviceSerialNo()
   */
  private void setDeviceSerialNo(final String deviceSerialNo) {
    if (null == deviceSerialNo) {
      throw new IllegalArgumentException(resourceBundleHolder_.get().getString("serial.is.null"));
    }

    if (deviceSerialNo.length() > Limits.SERIAL_NUMBER_MAXLEN) {
      throw new IllegalArgumentException(resourceBundleHolder_.get().getString("serial.too.long"));
    }

    this.deviceSerialNo = deviceSerialNo;
  }

  /**
   * The time at which the DI session ended.
   */
  public Timestamp getDiEnd() {
    return diEnd;
  }

  /**
   * set diEnd.
   *
   * @see DeviceState#getDiEnd()
   */
  public void setDiEnd(final Timestamp diEnd) {
    this.diEnd = diEnd;
  }

  /**
   * The time at which the DI session began.
   */
  public Timestamp getDiStart() {
    return diStart;
  }

  /**
   * set diStart.
   *
   * @see DeviceState#getDiStart()
   */
  private void setDiStart(final Timestamp diStart) {
    this.diStart = diStart;
  }

  /**
   * The internal state of the session.
   *
   * <p>This field isn't meant for human consumption.
   */
  public String getSessionData() {
    return sessionData;
  }

  /**
   * set session data.
   *
   * @see DeviceState#getSessionData()
   */
  public void setSessionData(final String sessionData) {
    this.sessionData = sessionData;
  }

  /**
   * The current status of this session.
   *
   * <p>Positive integers mean 'complete and healthy'.
   * Zero means 'in progress'.
   * Negative integers mean 'terminated due to error'.
   */
  public Integer getStatus() {
    return status;
  }

  /**
   * set status.
   *
   * @see DeviceState#getStatus()
   */
  public void setStatus(final Integer status) {
    this.status = status;
  }
}
