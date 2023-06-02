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
package org.apache.fineract.infrastructure.reportmailingjob.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.reportmailingjob.ReportMailingJobConstants;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobConfigurationData;
import org.apache.fineract.infrastructure.reportmailingjob.data.ReportMailingJobEmailData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class ReportMailingJobEmailServiceImpl implements ReportMailingJobEmailService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportMailingJobEmailServiceImpl.class);
    private final ReportMailingJobConfigurationReadPlatformService reportMailingJobConfigurationReadPlatformService;

    /**
     * ReportMailingJobEmailServiceImpl constructor
     **/
    @Autowired
    public ReportMailingJobEmailServiceImpl(
            final ReportMailingJobConfigurationReadPlatformService reportMailingJobConfigurationReadPlatformService) {
        this.reportMailingJobConfigurationReadPlatformService = reportMailingJobConfigurationReadPlatformService;

    }

    @Override
    public void sendEmailWithAttachment(ReportMailingJobEmailData reportMailingJobEmailData) {
        Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection;
        try {
            // get all ReportMailingJobConfiguration objects from the database
            reportMailingJobConfigurationDataCollection = this.reportMailingJobConfigurationReadPlatformService
                    .retrieveAllReportMailingJobConfigurations();

            JavaMailSenderImpl javaMailSenderImpl = new JavaMailSenderImpl();
            javaMailSenderImpl.setHost(this.getGmailSmtpServer(reportMailingJobConfigurationDataCollection));
            javaMailSenderImpl.setPort(this.getGmailSmtpPort(reportMailingJobConfigurationDataCollection));
            javaMailSenderImpl.setUsername(this.getGmailSmtpUsername(reportMailingJobConfigurationDataCollection));
            javaMailSenderImpl.setPassword(this.getGmailSmtpPassword(reportMailingJobConfigurationDataCollection));
            javaMailSenderImpl.setJavaMailProperties(this.getJavaMailProperties(reportMailingJobConfigurationDataCollection));

            MimeMessage mimeMessage = javaMailSenderImpl.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setTo(reportMailingJobEmailData.getTo());
            mimeMessageHelper.setText(reportMailingJobEmailData.getText());
            mimeMessageHelper.setSubject(reportMailingJobEmailData.getSubject());

            if (reportMailingJobEmailData.getAttachment() != null) {
                mimeMessageHelper.addAttachment(reportMailingJobEmailData.getAttachment().getName(),
                        reportMailingJobEmailData.getAttachment());
            }

            javaMailSenderImpl.send(mimeMessage);
        } catch (MessagingException e) {
            // handle the exception
            LOG.error("Problem occurred in sendEmailWithAttachment function", e);
        }
    }

    /**
     * @return Properties object containing JavaMail properties
     **/
    private Properties getJavaMailProperties(Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection) {
        Properties properties = new Properties();

        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.trust", this.getGmailSmtpServer(reportMailingJobConfigurationDataCollection));

        return properties;
    }

    /**
     * get a report mailing job configuration object by name from collection of objects
     *
     * @param name
     *            -- the value of the name property
     * @return ReportMailingJobConfigurationData object
     **/
    private ReportMailingJobConfigurationData getReportMailingJobConfigurationData(
            final Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection, final String name) {
        ReportMailingJobConfigurationData reportMailingJobConfigurationData = null;

        if (reportMailingJobConfigurationDataCollection != null && !reportMailingJobConfigurationDataCollection.isEmpty()) {
            for (ReportMailingJobConfigurationData reportMailingJobConfigurationDataObject : reportMailingJobConfigurationDataCollection) {
                String configurationName = reportMailingJobConfigurationDataObject.getName();

                if (!StringUtils.isEmpty(configurationName) && configurationName.equals(name)) {
                    reportMailingJobConfigurationData = reportMailingJobConfigurationDataObject;
                    break;
                }
            }
        }

        return reportMailingJobConfigurationData;
    }

    /**
     * @return Gmail smtp server name
     **/
    private String getGmailSmtpServer(Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection) {
        final ReportMailingJobConfigurationData reportMailingJobConfigurationData = this.getReportMailingJobConfigurationData(
                reportMailingJobConfigurationDataCollection, ReportMailingJobConstants.GMAIL_SMTP_SERVER);

        return (reportMailingJobConfigurationData != null) ? reportMailingJobConfigurationData.getValue() : null;
    }

    /**
     * @return Gmail smtp server port number
     **/
    private Integer getGmailSmtpPort(Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection) {
        final ReportMailingJobConfigurationData reportMailingJobConfigurationData = this.getReportMailingJobConfigurationData(
                reportMailingJobConfigurationDataCollection, ReportMailingJobConstants.GMAIL_SMTP_PORT);
        final String portNumber = (reportMailingJobConfigurationData != null) ? reportMailingJobConfigurationData.getValue() : null;

        return (portNumber != null) ? Integer.parseInt(portNumber) : null;
    }

    /**
     * @return Gmail smtp username
     **/
    private String getGmailSmtpUsername(Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection) {
        final ReportMailingJobConfigurationData reportMailingJobConfigurationData = this.getReportMailingJobConfigurationData(
                reportMailingJobConfigurationDataCollection, ReportMailingJobConstants.GMAIL_SMTP_USERNAME);

        return (reportMailingJobConfigurationData != null) ? reportMailingJobConfigurationData.getValue() : null;
    }

    /**
     * @return Gmail smtp password
     **/
    private String getGmailSmtpPassword(Collection<ReportMailingJobConfigurationData> reportMailingJobConfigurationDataCollection) {
        final ReportMailingJobConfigurationData reportMailingJobConfigurationData = this.getReportMailingJobConfigurationData(
                reportMailingJobConfigurationDataCollection, ReportMailingJobConstants.GMAIL_SMTP_PASSWORD);

        return (reportMailingJobConfigurationData != null) ? reportMailingJobConfigurationData.getValue() : null;
    }
}
