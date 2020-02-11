// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

/**
 * SDO type 'OwnershipVoucher'.
 */
public class Voucher {

  private final String oh;
  private final String hmac;
  private final String dc;
  private final List<String> en;

  /**
   * Constructor.
   *
   * @param oh   The encoded 'oh' field.
   * @param hmac The encoded 'hmac' field.
   * @param dc   The encoded 'dc' field.
   * @param en   The encoded 'en' list.
   */
  public Voucher(final String oh, final String hmac, final String dc, final List<String> en) {
    this.oh = oh;
    this.hmac = hmac;
    this.dc = dc;
    this.en = Collections.unmodifiableList(en);
  }

  /**
   * Decode a string-encoded Voucher.
   *
   * @param encoded The encoded Voucher.
   *
   * @return The decoded Voucher.
   */
  public static Voucher of(String encoded) {
    Pattern pat = Pattern.compile(
        "^\\{"
        + "\"sz\":(.*?)"
        + ",\"oh\":(.*?)"
        + ",\"hmac\":(.*?)"
        + "(?:,\"dc\":(.*?))?"
        + ",\"en\":(.*?)"
        + "}$"
    );
    Matcher matcher = pat.matcher(encoded);
    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }

    final int sz = IntegerCodec.decode(matcher.group(1), 8);
    return new Voucher(
      matcher.group(2),
      matcher.group(3),
      matcher.group(4),
      decodeEntryList(sz, matcher.group(5)));
  }

  static List<String> decodeEntryList(final int numEntries, final String encoded) {
    List<String> entries = new ArrayList<>();

    Matcher matcher = Pattern.compile("^\\[(.*)]$").matcher(encoded);
    if (!matcher.matches()) {
      throw new IllegalArgumentException();
    }
    String s = matcher.group(1);

    int begin = -1;
    int cursor = 0;
    int depth = 0;
    for (; cursor < s.length(); cursor++) {
      char c = s.charAt(cursor);
      if ('{' == c) {
        if (0 == depth) {
          begin = cursor;
        }
        depth++;
      } else if ('}' == c) {
        depth--;
      } else if (',' == c && 0 == depth) {
        entries.add(s.substring(begin, cursor));
      }
    }

    if (0 <= begin) {
      entries.add(s.substring(begin, cursor));
    }

    if (numEntries != entries.size()) {
      throw new IllegalArgumentException(
        "expected " + numEntries + " voucher entries but got " + entries.size());
    }
    return entries;
  }

  static String encodeEntryList(final List<String> en) {

    StringBuilder sb = new StringBuilder();
    String separator = "";

    sb.append("[");

    for (String s : en) {
      sb.append(separator).append(s);
      separator = ",";
    }

    sb.append("]");

    return sb.toString();
  }

  String encodeEntryList() {
    return encodeEntryList(getEn());
  }

  /**
   * Assigns this voucher to a new recipient.
   *
   * @param recipientKey The recipient's public key.
   * @param signer       A {@link Signature} object initialized for signing.
   */
  public Voucher assign(PublicKey recipientKey, Signature signer) {

    PublicKey ownerKey = getOwnerKey();

    if (!isOwner(ownerKey, signer)) {
      throw new SignerNotOwnerException();
    }

    KeyType keyType = KeyUtils.toType(ownerKey);
    final CryptoLevel cryptoLevel = CryptoLevel.of(keyType);
    DigestWrapper digest = cryptoLevel.buildDigestWrapper();

    if (getEn().isEmpty()) {
      digest.update(getOh().getBytes(StandardCharsets.US_ASCII));
      digest.update(getHmac().getBytes(StandardCharsets.US_ASCII));
    } else {
      final VoucherEntry ve = VoucherEntry.of(getEn().get(getEn().size() - 1));
      final String vb = ve.getBo();
      digest.update(vb.getBytes(StandardCharsets.US_ASCII));
    }
    final String hp = new HashCodec().encode(digest.doFinal());

    final VoucherHeader header = VoucherHeader.of(getOh());
    digest.reset();
    digest.update(header.getGuid().getBytes(StandardCharsets.US_ASCII));
    digest.update(header.getDeviceInfo().getBytes(StandardCharsets.US_ASCII));
    final String hc = new HashCodec().encode(digest.doFinal());

    final String pk = KeyCodec.encode(recipientKey);
    final String bo = new VoucherEntry.Body(hp, hc, pk).toString();

    final String sg;
    try {
      signer.update(bo.getBytes(StandardCharsets.US_ASCII));
      sg = SignatureCodec.encode(signer.sign());
    } catch (SignatureException e) {
      throw new RuntimeException(e); // bug smell - signature should be initialized by now
    }

    final List<String> en = new LinkedList<>(getEn());
    en.add(new VoucherEntry(bo, sg).toString());

    return new Voucher(getOh(), getHmac(), getDc(), en);
  }

  /**
   * PM.OwnershipVoucher.dc
   */
  public String getDc() {
    return this.dc;
  }

  /**
   * PM.OwnershipVoucher.en
   */
  public List<String> getEn() {
    return this.en;
  }

  /**
   * PM.OwnershipVoucher.hmac
   */
  public String getHmac() {
    return this.hmac;
  }

  /**
   * PM.OwnershipVoucher.oh
   */
  public String getOh() {
    return this.oh;
  }

  /**
   * Returns the owner public key.
   *
   * @return The public key of the current owner of this voucher.
   */
  public PublicKey getOwnerKey() {

    final PublicKey ownerKey;

    if (getEn().isEmpty()) {
      ownerKey = KeyCodec.decode(VoucherHeader.of(oh).getPublicKey());

    } else {
      VoucherEntry ve = VoucherEntry.of(getEn().get(getEn().size() - 1));
      VoucherEntry.Body bo = VoucherEntry.Body.of(ve.getBo());
      ownerKey = KeyCodec.decode(bo.getPk());
    }

    return ownerKey;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{")
      .append("\"sz\":")
      .append(IntegerCodec.encode(getEn().size(), 8))
      .append(",\"oh\":")
      .append(getOh())
      .append(",\"hmac\":")
      .append(getHmac());

    if (null != getDc()) {
      stringBuilder.append(",\"dc\":").append(getDc());
    }

    stringBuilder.append(",\"en\":");
    stringBuilder.append(encodeEntryList(getEn()));

    stringBuilder.append("}");

    return stringBuilder.toString();
  }

  /**
   * Tests if the provided signer belongs to the owner of this voucher.
   *
   * @return true if the signer matches the voucher, false otherwise.
   */
  public boolean isOwner(Signature signer) {

    return isOwner(getOwnerKey(), signer);
  }

  private boolean isOwner(PublicKey ownerKey, Signature signer) {

    final int nonceSize = 8;
    byte[] nonce = new byte[nonceSize];
    ThreadLocalRandom.current().nextBytes(nonce); // not used for crypto

    try {
      signer.update(nonce);
      byte[] signature = signer.sign();

      Signature verifier =
          Signature.getInstance(signer.getAlgorithm(), BouncyCastleSingleton.INSTANCE);
      verifier.initVerify(ownerKey);
      verifier.update(nonce);
      return verifier.verify(signature);

    } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
      LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
      return false;
    }
  }
}
