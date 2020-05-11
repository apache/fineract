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


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.fineract.infrastructure.campaigns.email.EmailApiConstants;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailMessageWithAttachmentData;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailConfiguration;
import org.apache.fineract.infrastructure.campaigns.email.domain.EmailConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
            javaMailSenderImpl.setHost(this.getEmailSmtpServer());
            javaMailSenderImpl.setPort(this.getEmailSmtpPort());
            javaMailSenderImpl.setUsername(this.getEmailSmtpUsername());
            javaMailSenderImpl.setPassword(this.getEmailSmtpPassword());
            javaMailSenderImpl.setJavaMailProperties(this.getJavaMailProperties());

            MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);


            mimeMessageHelper.setFrom(new InternetAddress(this.getEmailFromEmail(), this.getEmailFromName()));
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

        }catch(MessagingException| UnsupportedEncodingException e){

        }

    }


    private String getEmailSmtpServer(){
        final EmailConfiguration emailSmtpServer = this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_SMTP_SERVER);
        return (emailSmtpServer !=null) ? emailSmtpServer.getValue() : null;
    }
    private Integer getEmailSmtpPort(){
        final EmailConfiguration emailSmtpPort = this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_SMTP_PORT);
        return (emailSmtpPort !=null) ? Integer.parseInt(emailSmtpPort.getValue()) : null;
    }
    private String getEmailSmtpUsername(){
        final EmailConfiguration emailSmtpUsername = this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_SMTP_USERNAME);
        return (emailSmtpUsername !=null) ? emailSmtpUsername.getValue() : null;
    }

    private String getEmailSmtpPassword(){
        final EmailConfiguration emailSmtpPassword = this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_SMTP_PASSWORD);
        return (emailSmtpPassword !=null) ? emailSmtpPassword.getValue() : null;
    }

    private String getEmailFromEmail(){
        final EmailConfiguration emailFromEmail= this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_FROM_EMAIL);
        return (emailFromEmail !=null) ? emailFromEmail.getValue() : null;
    }

    private String getEmailFromName(){
        final EmailConfiguration emailFromName= this.emailConfigurationRepository.findByName(EmailApiConstants.EMAIL_FROM_NAME);
        return (emailFromName !=null) ? emailFromName.getValue() : null;
    }

    private Properties getJavaMailProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", this.getEmailSmtpServer());

        return properties;
    }
}
