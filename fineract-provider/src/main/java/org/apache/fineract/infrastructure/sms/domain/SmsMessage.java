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
package org.apache.fineract.infrastructure.sms.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.sms.SmsApiConstants;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.joda.time.LocalDate;

@Entity
@Table(name = "sms_messages_outbound")
public class SmsMessage extends AbstractPersistableCustom<Long> {

    @Column(name = "external_id", nullable = true)
    private String externalId;

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
    @JoinColumn(name = "campaign_id", nullable = true)
    private SmsCampaign smsCampaign;

    @Column(name = "status_enum", nullable = false)
    private Integer statusType;

    @Column(name = "mobile_no", nullable = false, length = 50)
    private String mobileNo;

    @Column(name = "message", nullable = false)
    private String message;

//    @Column(name = "provider_id", nullable = true)
//    private Long providerId;
//
//    @Column(name = "campaign_name", nullable = true)
//    private String campaignName;

    @Column(name = "submittedon_date", nullable = true)
    @Temporal(TemporalType.DATE)
    private Date submittedOnDate;

    @Column(name = "delivered_on_date", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date deliveredOnDate;

    public static SmsMessage pendingSms(final String externalId, final Group group, final Client client, final Staff staff,
            final String message, final String mobileNo, final SmsCampaign smsCampaign) {
        return new SmsMessage(externalId, group, client, staff, SmsMessageStatusType.PENDING, message, mobileNo, smsCampaign);
    }

    public static SmsMessage sentSms(final String externalId, final Group group, final Client client, final Staff staff,
            final String message, final String mobileNo, final SmsCampaign smsCampaign) {
        return new SmsMessage(externalId, group, client, staff, SmsMessageStatusType.WAITING_FOR_DELIVERY_REPORT, message, mobileNo, smsCampaign);
    }

    public static SmsMessage instance(String externalId, final Group group, final Client client, final Staff staff,
            final SmsMessageStatusType statusType, final String message, final String mobileNo, final SmsCampaign smsCampaign) {

        return new SmsMessage(externalId, group, client, staff, statusType, message, mobileNo, smsCampaign);
    }

    protected SmsMessage() {
        //
    }

    private SmsMessage(String externalId, final Group group, final Client client, final Staff staff, final SmsMessageStatusType statusType,
            final String message, final String mobileNo, final SmsCampaign smsCampaign) {
        this.externalId = externalId;
        this.group = group;
        this.client = client;
        this.staff = staff;
        this.statusType = statusType.getValue();
        this.mobileNo = mobileNo;
        this.message = message;
        this.smsCampaign = smsCampaign;
        this.submittedOnDate = LocalDate.now().toDate();
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(1);

        if (command.isChangeInStringParameterNamed(SmsApiConstants.messageParamName, this.message)) {
            final String newValue = command.stringValueOfParameterNamed(SmsApiConstants.messageParamName);
            actualChanges.put(SmsApiConstants.messageParamName, newValue);
            this.message = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public SmsCampaign getSmsCampaign() {
        return this.smsCampaign;
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

    public String getMobileNo() {
        return mobileNo;
    }

    public String getMessage() {
        return message;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public void setStatusType(final Integer statusType) {
        this.statusType = statusType;
    }

    public Date getSubmittedOnDate() {
        return this.submittedOnDate;
    }

    public Date getDeliveredOnDate() {
        return this.deliveredOnDate;
    }

    public void setDeliveredOnDate(final Date deliveredOnDate) {
        this.deliveredOnDate = deliveredOnDate;
    }
}