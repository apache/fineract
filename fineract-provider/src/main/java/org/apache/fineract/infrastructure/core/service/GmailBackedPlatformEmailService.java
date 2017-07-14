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

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.fineract.infrastructure.configuration.data.SMTPCredentialsData;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesPropertiesReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GmailBackedPlatformEmailService implements PlatformEmailService {
	
	private final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService;
	
	@Autowired
	public GmailBackedPlatformEmailService(final ExternalServicesPropertiesReadPlatformService externalServicesReadPlatformService){
		this.externalServicesReadPlatformService = externalServicesReadPlatformService;
	}

    @Override
    public void sendToUserAccount(String organisationName, String contactName,
                                  String address, String username, String unencodedPassword) {

	    final String subject = "Welcome " + contactName + " to " + organisationName;
	    final String body = "You are receiving this email as your email account: " +
                address + " has being used to create a user account for an organisation named [" +
                organisationName + "] on Mifos.\n" +
                "You can login using the following credentials:\nusername: " + username + "\n" +
                "password: " + unencodedPassword + "\n" +
                "You must change this password upon first log in using Uppercase, Lowercase, number and character.\n" +
                "Thank you and welcome to the organisation.";

	    final EmailDetail emailDetail = new EmailDetail(subject, body, address, contactName);
	    sendDefinedEmail(emailDetail);

    }

    @Override
    public void sendDefinedEmail(EmailDetail emailDetails) {
        final Email email = new SimpleEmail();
        final SMTPCredentialsData smtpCredentialsData = this.externalServicesReadPlatformService.getSMTPCredentials();
        final String authuserName = smtpCredentialsData.getUsername();

        final String authuser = smtpCredentialsData.getUsername();
        final String authpwd = smtpCredentialsData.getPassword();

        // Very Important, Don't use email.setAuthentication()
        email.setAuthenticator(new DefaultAuthenticator(authuser, authpwd));
        email.setDebug(false); // true if you want to debug
        email.setHostName(smtpCredentialsData.getHost());

        try {
            if(smtpCredentialsData.isUseTLS()){
                email.getMailSession().getProperties().put("mail.smtp.starttls.enable", "true");
            }
            email.setFrom(authuser, authuserName);

            email.setSubject(emailDetails.getSubject());
            email.setMsg(emailDetails.getBody());

            email.addTo(emailDetails.getAddress(), emailDetails.getContactName());
            email.send();
        } catch (EmailException e) {
            throw new PlatformEmailSendException(e);
        }
    }
}