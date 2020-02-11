// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * The JPA Entity containing SDO server settings.
 */
@Entity
@Table(name = "mt_server_settings")
public class ServerSettings {

  @Id
  @GeneratedValue
  Integer id;

  /**
   * The rendezvous info string.
   *
   * @see ServerSettings#getRendezvousInfo()
   */
  @Column(name = "rendezvous_info")
  @Lob
  String rendezvousInfo;

  /**
   * The certificate validity period.
   *
   * @see ServerSettings#getCertificateValidityPeriod()
   */
  @Column(name = "certificate_validity_period", length = 128)
  String certificateValidityPeriod;

  public ServerSettings() {
  }

  /**
   * Contains the server settings.
   *
   * @param id id
   * @param rendezvousInfo rendezvousInfo
   * @param certificateValidityPeriod certificateValidityPeriod
   */
  public ServerSettings(
      final Integer id,
      final String rendezvousInfo,
      final String certificateValidityPeriod) {

    this.id = id;
    this.rendezvousInfo = rendezvousInfo;
    this.certificateValidityPeriod = certificateValidityPeriod;
  }

  /**
   * The validity period for generated device certificates.
   *
   * <p>This is an ISO-8601 period string.
   */
  public String getCertificateValidityPeriod() {
    return this.certificateValidityPeriod;
  }

  /**
   * The SDO RendezvousInfo to be inserted in generated vouchers.
   *
   * <p>For convenience, RendezvousInfo is formatted as a space-separated list of URLs,
   * not as the SDO-internal RendezvousInfo format.
   *
   * <p>RendezvousInfo variables which are not implicit in a basic URL may be added
   * to the instruction via URL queries.  For example:
   * <code>
   * http://localhost:1234/?delaysec=5678
   * </code>
   */
  public String getRendezvousInfo() {
    return this.rendezvousInfo;
  }
}
