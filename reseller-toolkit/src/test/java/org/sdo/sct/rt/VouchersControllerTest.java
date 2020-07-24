// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.junit.jupiter.api.Test;
import org.sdo.sct.BouncyCastleSingleton;
import org.sdo.sct.CertPathCodec;
import org.sdo.sct.Hash;
import org.sdo.sct.HashCodec;
import org.sdo.sct.KeyFinder;
import org.sdo.sct.KeyStores;
import org.sdo.sct.KeyStoresFactory;
import org.sdo.sct.KeyType;
import org.sdo.sct.NoSuchKeyException;
import org.sdo.sct.PasswordCallbackFunction;
import org.sdo.sct.RendezvousInfo;
import org.sdo.sct.RendezvousInfoBuilder;
import org.sdo.sct.SignerNotOwnerException;
import org.sdo.sct.Voucher;
import org.sdo.sct.VoucherHeader;
import org.sdo.sct.domain.Customer;
import org.sdo.sct.domain.OwnershipVoucherEntry;
import org.sdo.sct.domain.OwnershipVoucherRepo;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class VouchersControllerTest {

  PasswordCallbackFunction pw = "123456"::toCharArray;

  @Test
  void testGet() throws Exception {

    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", BouncyCastleSingleton.INSTANCE);
    // create EC-384 key pair.
    kpg.initialize(new ECGenParameterSpec("secp384r1"), SecureRandom.getInstance("SHA1PRNG"));
    KeyPair kp = kpg.generateKeyPair();

    KeyStoresFactory keyStoresFactory =
        new KeyStoresFactory(pw, new ClassPathResource("org/sdo/sct/test.p12"));
    keyStoresFactory.setSingleton(false);
    KeyStores keyStores = keyStoresFactory.getObject();

    RendezvousInfo r = new RendezvousInfoBuilder().with(URI.create("http://localhost")).build();
    UUID g = UUID.randomUUID();
    String d = VouchersControllerTest.class.getName();

    VoucherHeader oh = new VoucherHeader(r, g, d, kp.getPublic(), new Hash.Null());
    Voucher voucher = new Voucher(oh.toString(), new HashCodec().encode(new Hash.Null()),
        new CertPathCodec()
            .encode(CertificateFactory.getInstance("X.509", BouncyCastleSingleton.INSTANCE)
                .generateCertPath(Collections.emptyList())),
        Collections.emptyList());
    String voucherText = voucher.toString();

    OwnershipVoucherRepo voucherRepo = mock(OwnershipVoucherRepo.class);
    KeyFinder keyFinder = mock(KeyFinder.class);
    String deviceSerial = "test";
    String customerId = "test";

    VouchersController vc = new VouchersController(voucherRepo, keyFinder, pw);
    OwnershipVoucherEntry vEntry = new OwnershipVoucherEntry(deviceSerial, voucherText, null);

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> vc.get(deviceSerial).call());
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());

    given(voucherRepo.findById(deviceSerial)).willReturn(Optional.of(vEntry));
    assertEquals(voucherText, vc.get(deviceSerial).call());

    vEntry.setCustomer(new Customer(0, customerId, null));

    given(voucherRepo.findById(deviceSerial)).willReturn(Optional.of(vEntry));
    assertThrows(IllegalArgumentException.class, () -> vc.get(deviceSerial).call());

    vEntry.setCustomer(new Customer(0, customerId, ""));

    given(voucherRepo.findById(deviceSerial)).willReturn(Optional.of(vEntry));
    assertThrows(IllegalArgumentException.class, () -> vc.get(deviceSerial).call());

    vEntry.setCustomer(
        new Customer(0, customerId, KeyType.ECDSA_P_384 + ":" + toX509(kp.getPublic())));

    given(voucherRepo.findById(deviceSerial)).willReturn(Optional.of(vEntry));
    assertThrows(NoSuchKeyException.class, () -> vc.get(deviceSerial).call());

    given(keyFinder.find(KeyType.ECDSA_P_384))
        .willReturn(new KeyFinder(keyStores, pw).find(KeyType.ECDSA_P_384));
    assertThrows(SignerNotOwnerException.class, () -> vc.get(deviceSerial).call());

    // test against multiple formatted sample keys.
    ClassPathResource resource = new ClassPathResource("org/sdo/sct/rt/sample-keys-input.txt");
    String content = new String(Files.readAllBytes(Paths.get(resource.getURI())));
    String[] entries = content.split(">>>");
    for (String entry : entries) {
      vEntry.setCustomer(new Customer(0, customerId, entry));
      assertThrows(SignerNotOwnerException.class, () -> vc.get(deviceSerial).call());
    }
  }

  private String toX509(PublicKey key) throws IOException {
    StringWriter sw = new StringWriter();
    try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
      pw.writeObject(key);
    }
    return sw.toString();
  }
}
