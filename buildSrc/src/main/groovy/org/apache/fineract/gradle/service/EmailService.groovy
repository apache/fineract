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
package org.apache.fineract.gradle.service

import org.apache.fineract.gradle.FineractPluginExtension
import org.apache.fineract.gradle.FineractPluginExtension.FineractPluginEmailParams
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class)

    String username
    String password
    Properties properties

    EmailService(FineractPluginExtension.FineractPluginConfigSmtp config) {
        this.username = config.username
        this.password = config.password

        this.properties = new Properties()
        this.properties.put("mail.smtp.host", config.host)
        this.properties.put("mail.smtp.auth", "true")
        this.properties.put("mail.smtp.starttls.enable", config.tls.toString())
        if(config.ssl) {
            this.properties.put("mail.smtp.port", "465")
            this.properties.put("mail.smtp.socketFactory.port", "465");
            this.properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        } else {
            this.properties.put("mail.smtp.port", "587")
        }
    }

    void send(FineractPluginEmailParams params) {
        log.warn("Sending email: ${params.from} -> ${params.to} - ${params.subject}")

        Session session = Session.getInstance(this.properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword())
            }
        })

        try {

            Message msg = new MimeMessage(session)
            msg.setFrom(new InternetAddress(params.from, params.name))
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(params.to))
            if(params.cc) {
                msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(params.cc))
            }
            if(params.bcc) {
                msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(params.bcc))
            }
            msg.setSubject(params.subject)
            msg.setText(params.message);

            Transport.send(msg);

            log.info("Done")

        } catch (MessagingException e) {
            log.error(e.toString(), e)
        }
    }
}
