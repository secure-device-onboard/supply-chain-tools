// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.rt;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.sdo.sct.BouncyCastleSingleton;
import org.sdo.sct.CryptoLevel;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyHandle;
import org.sdo.sct.KeyHandle.AutoCloseableKey;
import org.sdo.sct.KeyType;
import org.sdo.sct.KeyUtils;
import org.sdo.sct.NoSuchKeyException;
import org.sdo.sct.PasswordCallbackFunction;
import org.sdo.sct.ResourceBundleHolder;
import org.sdo.sct.Voucher;
import org.sdo.sct.VoucherHeader;
import org.sdo.sct.domain.Customer;
import org.sdo.sct.domain.OwnershipVoucherEntry;
import org.sdo.sct.domain.OwnershipVoucherRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@SuppressWarnings("unused")
class VouchersController {

  private static ResourceBundleHolder resourceBundleHolder_ =
      new ResourceBundleHolder(VouchersController.class.getName());

  private final OwnershipVoucherRepo ownershipVoucherRepo;
  private final PasswordCallbackFunction pwCallback;
  private final KeyFinder keyFinder;

  public VouchersController(
      final OwnershipVoucherRepo ownershipVoucherRepo,
      final KeyFinder keyFinder,
      final PasswordCallbackFunction pwCallback) {

    this.ownershipVoucherRepo = ownershipVoucherRepo;
    this.keyFinder = keyFinder;
    this.pwCallback = pwCallback;
  }

  static PublicKey getKey(KeyType type, Customer customer) throws
      InvalidKeySpecException, IOException, NoSuchAlgorithmException {

    final String pem = customer.getKey();
    if (null != pem && !pem.isBlank()) {
      final Map<String, String> keyMap =
          Arrays.stream(pem.split(",")).map(entry -> entry.split(":"))
              .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]));
      for (Map.Entry<String, String> keyEntry : keyMap.entrySet()) {
        // remove the common escape sequences /r, /t and leading/trailing whitespaces from the PEM
        // formatted public key.
        final String publicKeyAsPem =
            keyEntry.getValue().replaceAll("\t", "").replaceAll("\r", "").trim();
        try (PEMParser pemParser = new PEMParser(new StringReader(publicKeyAsPem))) {
          for (Object o = pemParser.readObject(); null != o; o = pemParser.readObject()) {
            if (o instanceof SubjectPublicKeyInfo) {
              SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) o;
              final KeyFactory keyFactory = KeyFactory.getInstance(
                  subjectPublicKeyInfo.getAlgorithm().getAlgorithm().toString(),
                  BouncyCastleSingleton.INSTANCE);
              final X509EncodedKeySpec subjectKeySpec =
                  new X509EncodedKeySpec(subjectPublicKeyInfo.getEncoded());
              PublicKey key = keyFactory.generatePublic(subjectKeySpec);
              if (type == KeyUtils.toType(key)) {
                return key;
              }
            } // SubjectPublicKeyInfo?
          } // foreach PEM object
        } // try with PEMParser
      }

      String format = resourceBundleHolder_.get().getString("key.wrong.type");
      throw new IllegalArgumentException(MessageFormat.format(format, type));
    } // pem non-null?

    String format = resourceBundleHolder_.get().getString("key.parse.error");
    throw new IOException(MessageFormat.format(format, type, pem));
  }

  private static boolean verifyKeyPair(PublicKey publicKey, KeyHandle privateKeyHandle) {

    try {
      return Objects.equals(publicKey, privateKeyHandle.getCertificate().getPublicKey());
    } catch (KeyStoreException e) {
      return false;
    }
  }

  @GetMapping(path = "/api/v1/vouchers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  Callable<String> get(@PathVariable String id) {
    return () -> get_(id);
  }

  private String get_(String id) throws IOException, KeyStoreException {

    final OwnershipVoucherEntry voucherEntry = ownershipVoucherRepo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    final Voucher voucher = Voucher.of(voucherEntry.getVoucher());
    final VoucherHeader oh = VoucherHeader.of(voucher.getOh());

    // If there's no customer assigned to the voucher, return it as-is.
    final Customer customer = voucherEntry.getCustomer();
    if (null == customer) {
      return voucherEntry.getVoucher();
    }

    // build a new voucher entry assigning the voucher to the identified customer

    // Since all keys must be of the same type and strength in the voucher,
    // we can figure out the key type by auditing the first key in the header.
    // Once we know the key type, we know the crypto level, which controls
    // digest and mac sizes.
    final KeyType keyType = KeyUtils.toType(voucher.getOwnerKey());

    PublicKey recipientKey;
    try {
      recipientKey = getKey(keyType, customer);

    } catch (InvalidKeySpecException | IOException e) {
      throw new IllegalArgumentException(e);

    } catch (NoSuchAlgorithmException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    KeyHandle signingKeyHandle = keyFinder.find(keyType)
        .orElseThrow(() -> new NoSuchKeyException(keyType));

    final Signature signer;
    try (AutoCloseableKey autoCloseableKey = signingKeyHandle.getKey(pwCallback)) {
      if (!(autoCloseableKey.getKey() instanceof PrivateKey)) {
        String format = resourceBundleHolder_.get().getString("key.not.private");
        throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          MessageFormat.format(format, signingKeyHandle.getAlias()));
      }

      signer = CryptoLevel.of(keyType).buildSignature(
        autoCloseableKey.getKey(), signingKeyHandle.getKeyStore().getProvider());
      signer.initSign((PrivateKey) autoCloseableKey.getKey());

    } catch (GeneralSecurityException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    Voucher newVoucher = voucher.assign(recipientKey, signer);
    return newVoucher.toString();
  }
}
