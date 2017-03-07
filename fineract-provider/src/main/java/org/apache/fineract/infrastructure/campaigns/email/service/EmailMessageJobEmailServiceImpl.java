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


import org.apache.fineract.infrastructure.campaigns.email.EmailApiConstants;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailConfiguration;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Service
public class EmailMessageJobEmailServiceImpl implements EmailMessageJobEmailService {

    private EmailConfigurationRepository emailConfigurationRepository;

    @Autowired
    private EmailMessageJobEmailServiceImpl(final EmailConfigurationRepository emailConfigurationRepository) {
        this.emailConfigurationRepository = emailConfigurationRepository;
    }

    @Override
    public void sendEmailWithAttachment(EmailMessageWithAttachmentData emailMessageWithAttachmentData) {
        try{
            JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
            javaMailSenderImpl.setHost(this.getGmailSmtpServer());
            javaMailSenderImpl.setPort(this.getGmailSmtpPort());
            javaMailSenderImpl.setUsername(this.getGmailSmtpUsername());
            javaMailSenderImpl.setPassword(this.getGmailSmtpPassword());
            javaMailSenderImpl.setJavaMailProperties(this.getJavaMailProperties());

            MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setTo(emailMessageWithAttachmentData.getTo());
            mimeMessageHelper.setText(emailMessageWithAttachmentData.getText());
            mimeMessageHelper.setSubject(emailMessageWithAttachmentData.getSubject());
            final List<File> attachments = emailMessageWithAttachmentData.getAttachments();
            if(attachments !=null && attachments.size() > 0){
                for(final File attachment : attachments){
                    if(attachment !=null){
                        mimeMessageHelper.addAttachment(attachment.getName(),attachment);
                    }
                }
            }

            javaMailSenderImpl.send(mimeMessage);

        }catch(MessagingException e){

        }

    }


    private String getGmailSmtpServer(){
        final EmailConfiguration gmailSmtpServer = this.emailConfigurationRepository.findByName(EmailApiConstants.SMTP_SERVER);
        return (gmailSmtpServer !=null) ? gmailSmtpServer.getValue() : null;
    }
    private Integer getGmailSmtpPort(){
        final EmailConfiguration gmailSmtpPort = this.emailConfigurationRepository.findByName(EmailApiConstants.SMTP_PORT);
        return (gmailSmtpPort !=null) ? Integer.parseInt(gmailSmtpPort.getValue()) : null;
    }
    private String getGmailSmtpUsername(){
        final EmailConfiguration gmailSmtpUsername = this.emailConfigurationRepository.findByName(EmailApiConstants.SMTP_USERNAME);
        return (gmailSmtpUsername !=null) ? gmailSmtpUsername.getValue() : null;
    }

    private String getGmailSmtpPassword(){
        final EmailConfiguration gmailSmtpPassword = this.emailConfigurationRepository.findByName(EmailApiConstants.SMTP_PASSWORD);
        return (gmailSmtpPassword !=null) ? gmailSmtpPassword.getValue() : null;
    }

    private Properties getJavaMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.trust", this.getGmailSmtpServer());

        return properties;
    }
}
