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
package org.apache.fineract.infrastructure.campaigns.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class EmailMessageJobEmailServiceImpl implements EmailMessageJobEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailMessageJobEmailServiceImpl.class);
    private final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService;

    @Override
    public void sendEmailWithAttachment(EmailMessageWithAttachmentData emailMessageWithAttachmentData) {
        final SMTPCredentialsData smtpCredentialsData = this.externalServicesReadPlatformService.getSMTPCredentials();
        try {
            JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
            javaMailSenderImpl.setHost(smtpCredentialsData.getHost());
            javaMailSenderImpl.setPort(Integer.parseInt(smtpCredentialsData.getPort()));
            javaMailSenderImpl.setUsername(smtpCredentialsData.getUsername());
            javaMailSenderImpl.setPassword(smtpCredentialsData.getPassword());
            javaMailSenderImpl
                    .setJavaMailProperties(this.getJavaMailProperties(smtpCredentialsData, javaMailSenderImpl.getJavaMailProperties()));

            MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(smtpCredentialsData.getFromEmail());
            mimeMessageHelper.setTo(emailMessageWithAttachmentData.getTo());
            mimeMessageHelper.setText(emailMessageWithAttachmentData.getText(), true);
            mimeMessageHelper.setSubject(emailMessageWithAttachmentData.getSubject());
            final List<File> attachments = emailMessageWithAttachmentData.getAttachments();
            if (attachments != null && attachments.size() > 0) {
                for (final File attachment : attachments) {
                    if (attachment != null) {
                        mimeMessageHelper.addAttachment(attachment.getName(), attachment);
                    }
                }
            }

            javaMailSenderImpl.send(mimeMessage);

        } catch (MessagingException e) {
            LOG.error("Could not send emai Problem occurred in sendEmailWithAttachment function", e);
        }

    }

    private Properties getJavaMailProperties(SMTPCredentialsData smtpCredentialsData, Properties properties) {
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.trust", smtpCredentialsData.getHost());
        if (smtpCredentialsData.isUseTLS()) {
            // Needs to disable startTLS if the port is 465 in order to send the email successfully when using the
            // smtp.gmail.com as the host
            if (smtpCredentialsData.getPort().equals("465")) {
                properties.put("mail.smtp.starttls.enable", "false");
            }
        }
        return properties;
    }
}
