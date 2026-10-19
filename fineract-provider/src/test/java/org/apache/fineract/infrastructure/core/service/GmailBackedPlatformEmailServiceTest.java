/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

class GmailBackedPlatformEmailServiceTest {

    private static final String FROM = "sender@localhost";
    private static final String TO = "recipient@localhost";

    private GmailBackedPlatformEmailService underTest;

    @BeforeEach
    void setUp() {
        ExternalServicesPropertiesReadPlatformService externalServices = mock(ExternalServicesPropertiesReadPlatformService.class);
        when(externalServices.getSMTPCredentials()).thenReturn(
                new SMTPCredentialsData().setUsername("user").setPassword("secret").setHost("localhost").setPort("25").setFromEmail(FROM));
        underTest = new GmailBackedPlatformEmailService(externalServices);
    }

    @Test
    void sendDefinedEmailSendsPlainTextByDefault() {
        try (MockedConstruction<JavaMailSenderImpl> senders = mockJavaMailSender()) {
            underTest.sendDefinedEmail(new EmailDetail("Subject", "Plain body", TO, "Contact"));

            JavaMailSenderImpl sender = senders.constructed().get(0);
            ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
            verify(sender).send(captor.capture());
            verify(sender, never()).send(any(MimeMessage.class));
            assertThat(captor.getValue().getFrom()).isEqualTo(FROM);
            assertThat(captor.getValue().getTo()).containsExactly(TO);
            assertThat(captor.getValue().getSubject()).isEqualTo("Subject");
            assertThat(captor.getValue().getText()).isEqualTo("Plain body");
        }
    }

    @Test
    void sendDefinedEmailSendsHtmlWhenRequested() throws Exception {
        String body = "<p>Dear José Müller, click <a href=\"https://example.org/reset\">here</a> – ₹100</p>";
        try (MockedConstruction<JavaMailSenderImpl> senders = mockJavaMailSender()) {
            underTest.sendDefinedEmail(new EmailDetail("Subject", body, TO, "Contact", true));

            JavaMailSenderImpl sender = senders.constructed().get(0);
            ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(sender).send(captor.capture());
            verify(sender, never()).send(any(SimpleMailMessage.class));
            MimeMessage sent = captor.getValue();
            sent.saveChanges();
            ByteArrayOutputStream wire = new ByteArrayOutputStream();
            sent.writeTo(wire);
            MimeMessage message = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(wire.toByteArray()));
            assertThat(message.getContentType()).startsWith("text/html").containsIgnoringCase("charset=UTF-8");
            assertThat(message.getContent()).isEqualTo(body);
            assertThat(message.getFrom()).containsExactly(new InternetAddress(FROM));
            assertThat(message.getAllRecipients()).containsExactly(new InternetAddress(TO));
            assertThat(message.getSubject()).isEqualTo("Subject");
        }
    }

    private MockedConstruction<JavaMailSenderImpl> mockJavaMailSender() {
        return mockConstruction(JavaMailSenderImpl.class, (sender, context) -> {
            when(sender.getJavaMailProperties()).thenReturn(new Properties());
            when(sender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));
        });
    }
}
