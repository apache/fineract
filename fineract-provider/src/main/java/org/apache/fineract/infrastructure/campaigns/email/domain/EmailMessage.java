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
package org.apache.fineract.infrastructure.campaigns.email.domain;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.campaigns.email.EmailApiConstants;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "scheduled_email_messages_outbound")
public class EmailMessage extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = true)
    private Staff staff;

    @ManyToOne
    @JoinColumn(name = "email_campaign_id", nullable = true)
    private EmailCampaign emailCampaign;

    @Column(name = "status_enum", nullable = false)
    private Integer statusType;

    @Column(name = "email_address", nullable = false, length = 50)
    private String emailAddress;

    @Column(name = "email_subject", nullable = false, length = 50)
    private String emailSubject;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "campaign_name", nullable = true)
    private String campaignName;

    @Column(name = "submittedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date submittedOnDate;

    @Column(name = "error_message")
    private String errorMessage;


    public static EmailMessage pendingEmail(final Group group, final Client client, final Staff staff,final EmailCampaign emailCampaign, final String emailSubject, final String message,
                                        final String emailAddress, final String campaignName) {
        return new EmailMessage(group, client, staff,emailCampaign,EmailMessageStatusType.PENDING, emailSubject, message, emailAddress,campaignName);
    }
    
    public static EmailMessage instance(final Group group, final Client client, final Staff staff, final EmailCampaign emailCampaign, final EmailMessageStatusType statusType,
                                      final String emailSubject, final String message, final String sourceAddress, final String emailAddress, final String campaignName) {
    	return new EmailMessage(group, client, staff,emailCampaign, statusType, emailSubject, message, emailAddress, campaignName);
    }

    protected EmailMessage() {
        //
    }

    private EmailMessage(final Group group, final Client client, final Staff staff, final EmailCampaign emailCampaign, final EmailMessageStatusType statusType,
            final String emailSubject, final String message, final String emailAddress, final String campaignName) {
    	this.group = group;
        this.client = client;
        this.staff = staff;
        this.emailCampaign = emailCampaign;
        this.statusType = statusType.getValue();
        this.emailAddress = emailAddress;
        this.emailSubject = emailSubject;
        this.message = message;
        this.campaignName = campaignName;
        this.submittedOnDate = LocalDate.now().toDate();
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        if (command.isChangeInStringParameterNamed(EmailApiConstants.messageParamName, this.message)) {
            final String newValue = command.stringValueOfParameterNamed(EmailApiConstants.messageParamName);
            actualChanges.put(EmailApiConstants.messageParamName, newValue);
            this.message = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }
    

    public Group getGroup() {
    	return group;
    }
    
    public Client getClient() {
    	return client;
    }
    
    public Staff getStaff() {
    	return staff;
    }
    
    public Integer getStatusType() {
    	return statusType;
    }


    public String getEmailAddress() {return this.emailAddress;}

    public String getEmailSubject() {return emailSubject; }
    
    public String getMessage() {
    	return message;
    }
    

    public void setStatusType(final Integer statusType) {
    	this.statusType = statusType;
    }

    public String getCampaignName() {
        return this.campaignName;
    }

    public Date getSubmittedOnDate() {
        return this.submittedOnDate;
    }

    public EmailCampaign getEmailCampaign() {return this.emailCampaign;}

    public void updateErrorMessage(final String errorMessage) {this.errorMessage = errorMessage;}

    public boolean isPending(){ return EmailMessageStatusType.fromInt(this.statusType).isPending();}
    public boolean isSent(){ return EmailMessageStatusType.fromInt(this.statusType).isSent();}
}