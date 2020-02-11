// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.security.PublicKey;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SDO OwnershipVoucher 'oh' header.
 */
public class VoucherHeader {

  private final String pe;
  private final String rendezvous;
  private final String guid;
  private final String deviceInfo;
  private final String publicKey;
  private final String certChainHash;

  /**
   * Constructor.
   *
   * @param rendezvous   the {@link RendezvousInfo}, PM.OwnershipVoucher.oh.rendezvous.
   * @param guid   the GUID, PM.OwnershipVoucher.oh.guid.
   * @param deviceInfo   the DeviceInfo, PM.OwnershipVoucher.oh.deviceInfo.
   * @param publicKey  the manufacturer's public key, PM.OwnershipVoucher.oh.publicKey.
   * @param certChainHash the hash of the device certificate chain,
   *                      PM.OwnershipVoucher.oh.certChainHash.
   */
  public VoucherHeader(RendezvousInfo rendezvous,
                       UUID guid,
                       String deviceInfo,
                       PublicKey publicKey,
                       Hash certChainHash) {

    this(
        KeyUtils.toEncoding(KeyUtils.toType(publicKey)).toString(),
        rendezvous.toString(),
        UuidCodec.encode(guid),
        StringCodec.encode(deviceInfo),
        KeyCodec.encode(publicKey),
        null == certChainHash ? null : new HashCodec().encode(certChainHash));
  }

  private VoucherHeader(String pe,
                        String rendezvous,
                        String guid,
                        String deviceInfo,
                        String publicKey,
                        String certChainHash) {
    this.pe = pe;
    this.rendezvous = rendezvous;
    this.guid = guid;
    this.deviceInfo = deviceInfo;
    this.publicKey = publicKey;
    this.certChainHash = certChainHash;
  }

  /**
   * Decode a string-encoded header.
   *
   * @param encoded The encoded header.
   *
   * @return The decoded header.
   */
  public static VoucherHeader of(String encoded) {
    Pattern pat = Pattern.compile(
        "^\\{"
        + "\"pv\":113"
        + ",\"pe\":(.*?)"
        + ",\"r\":(.*?)"
        + ",\"g\":(.*?)"
        + ",\"d\":(.*?)"
        + ",\"pk\":(.*?)"
        + "(?:,\"hdc\":(.*?))?"
        + "}$"
    );
    Matcher matcher = pat.matcher(encoded);
    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    return new VoucherHeader(
      matcher.group(1),
      matcher.group(2),
      matcher.group(3),
      matcher.group(4),
      matcher.group(5),
      matcher.group(6));
  }

  /**
   * PM.OwnershipVoucher.oh.deviceInfo
   */
  public String getDeviceInfo() {
    return this.deviceInfo;
  }

  /**
   * PM.OwnershipVoucher.oh.guid
   */
  public String getGuid() {
    return this.guid;
  }

  /**
   * PM.OwnershipVoucher.oh.certChainHash
   */
  public String getCertChainHash() {
    return this.certChainHash;
  }

  /**
   * PM.OwnershipVoucher.oh.pe
   */
  public String getPe() {
    return this.pe;
  }

  /**
   * PM.OwnershipVoucher.oh.publicKey
   */
  public String getPublicKey() {
    return this.publicKey;
  }

  /**
   * PM.OwnershipVoucher.oh.rendezvous
   */
  public String getRendezvous() {
    return this.rendezvous;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "{\"pv\":113,\"pe\":" + getPe()
      + ",\"r\":" + getRendezvous()
      + ",\"g\":" + getGuid()
      + ",\"d\":\"" + getDeviceInfo()
      + "\",\"pk\":" + getPublicKey()
      + (null == getCertChainHash() ? "" : ",\"hdc\":" + getCertChainHash())
      + "}";
  }
}
