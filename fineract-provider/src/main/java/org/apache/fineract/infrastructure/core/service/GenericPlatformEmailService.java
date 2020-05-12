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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class GenericPlatformEmailService implements PlatformEmailService {

    private final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService;

    @Autowired
    public GenericPlatformEmailService(final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService){
        this.externalServicesReadPlatformService = externalServicesReadPlatformService;
    }

    @Override
    public void sendToUserAccount(String organisationName, String contactName,
                                  String address, String username, String unencodedPassword) {

        final String subject = "Welcome " + contactName + " to " + organisationName;
        final String body = "You are receiving this email as your email account: " +
                address + " has being used to create a user account for an organisation named [" +
                organisationName + "] on Fineract.\n" +
                "You can login using the following credentials:\nusername: " + username + "\n" +
                "password: " + unencodedPassword + "\n" +
                "You must change this password upon first log in using Uppercase, Lowercase, number and character.\n" +
                "Thank you and welcome to the organisation.";

        final EmailDetail emailDetail = new EmailDetail(subject, body, address, contactName);
        sendDefinedEmail(emailDetail);
    }

    @Override
    public void sendDefinedEmail(EmailDetail emailDetails) {
        final SMTPCredentialsData smtpCredentialsData = this.externalServicesReadPlatformService.getSMTPCredentials();
        try{
            JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();

            javaMailSenderImpl.setHost(smtpCredentialsData.getHost());
            javaMailSenderImpl.setPort(Integer.parseInt(smtpCredentialsData.getPort()));
            javaMailSenderImpl.setUsername(smtpCredentialsData.getUsername());
            javaMailSenderImpl.setPassword(smtpCredentialsData.getPassword());
            javaMailSenderImpl.setJavaMailProperties(this.getJavaMailProperties(smtpCredentialsData));

            if (emailDetails.getAttachments().isEmpty()){
                SimpleMailMessage message = new SimpleMailMessage();

                message.setFrom(new InternetAddress(smtpCredentialsData.getFromEmail(), smtpCredentialsData.getFromName()).toString());
                message.setTo(new InternetAddress(emailDetails.getAddress(), emailDetails.getContactName()).toString());
                message.setSubject(emailDetails.getSubject());
                message.setText(emailDetails.getBody());
                javaMailSenderImpl.send(message);

            } else {
                MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();

                // use the true flag to indicate you need a multipart message
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

                mimeMessageHelper.setFrom(new InternetAddress(smtpCredentialsData.getFromEmail(), smtpCredentialsData.getFromName()));
                mimeMessageHelper.setTo(new InternetAddress(emailDetails.getAddress(), emailDetails.getContactName()));
                mimeMessageHelper.setText(emailDetails.getBody());
                mimeMessageHelper.setSubject(emailDetails.getSubject());
                final List<File> attachments = emailDetails.getAttachments();
                if(!attachments.isEmpty()){
                   for(final File attachment : attachments){
                        if(attachment !=null){
                            mimeMessageHelper.addAttachment(attachment.getName(), attachment);
                        }
                    }
                }
                javaMailSenderImpl.send(mimeMessage);
            }
        }catch(MessagingException | UnsupportedEncodingException e){
            throw new PlatformEmailSendException(e);
        }
    }

    private Properties getJavaMailProperties(SMTPCredentialsData smtpCredentialsData) {
        Properties properties = new Properties();

        if(smtpCredentialsData.isUseTLS()) {
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.starttls.enable", "true");
            properties.setProperty("mail.smtp.ssl.trust", smtpCredentialsData.getHost());
        }

        return properties;
    }
}