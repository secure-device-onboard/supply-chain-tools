// Copyright 2019 Intel Corporation
// SPDX-License-Identifier: Apache 2.0

package org.sdo.sct.app;

import org.sdo.sct.PasswordCallbackFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("org.sdo.sct org.sdo.sct.rt")
@EnableJpaRepositories("org.sdo.sct.domain")
@EntityScan("org.sdo.sct.domain")
public class ResellerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(ResellerApplication.class, args);
  }

  @Bean
  @SuppressWarnings("unused")
  PasswordCallbackFunction keyStorePasswordCallback(@Value("${sdo.keystore.password:}") String pw) {
    return () -> pw.toCharArray();
  }
}
