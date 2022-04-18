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
package org.apache.fineract.portfolio.client.domain;

import com.google.gson.JsonObject;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_client_contact_information")
public class ClientContactInformation extends AbstractAuditableCustom {

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "contact_type_id", nullable = false)
    private CodeValue contactType;

    @Column(name = "contact_key", length = 1000)
    private String contactKey;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "current")
    private Boolean current;

    @Temporal(TemporalType.DATE)
    @Column(name = "created_date")
    private Date createdDate;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "createdby_id", nullable = true)
    private AppUser createdBy;

    public static ClientContactInformation fromJson(final Client client, final CodeValue contactType, final JsonCommand command,
            final AppUser appUser) {
        final String contactKey = command.stringValueOfParameterNamed(ClientApiConstants.contactKeyParamName);
        final String status = command.stringValueOfParameterNamed(ClientApiConstants.statusParamName);
        final Boolean currentContact = command.booleanObjectValueOfParameterNamed(ClientApiConstants.currentContactParamName);
        return new ClientContactInformation(client, contactType, contactKey, status, currentContact, appUser);
    }

    public static ClientContactInformation fromJson(final Client client, final CodeValue contactType, final JsonObject jsonObject,
            final AppUser appUser) {
        final String contactKey = jsonObject.get(ClientApiConstants.contactKeyParamName).getAsString();
        final String status = jsonObject.get(ClientApiConstants.statusParamName).getAsString();
        boolean currentContact = false;
        if (jsonObject.get(ClientApiConstants.currentContactParamName) != null) {
            currentContact = jsonObject.get(ClientApiConstants.currentContactParamName).getAsBoolean();
        }
        return new ClientContactInformation(client, contactType, contactKey, status, currentContact, appUser);
    }

    protected ClientContactInformation() {
        //
    }

    private ClientContactInformation(final Client client, final CodeValue contactType, final String contactKey, final String statusName,
            final Boolean currentContact, final AppUser appUser) {
        this.client = client;
        this.contactType = contactType;
        this.contactKey = StringUtils.defaultIfEmpty(contactKey, null);
        ClientIdentifierStatus statusEnum = ClientIdentifierStatus.valueOf(statusName.toUpperCase());
        this.current = currentContact;
        this.status = statusEnum.getValue();
        this.createdBy = appUser;
        this.createdDate = new Date();
    }

    public Map<String, Object> update(final CodeValue contactType, final JsonCommand command, final AppUser appUser) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        if (command.isChangeInLongParameterNamed(ClientApiConstants.contactTypeIdParamName, this.contactType.getId())) {
            final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.contactTypeIdParamName);
            actualChanges.put(ClientApiConstants.contactTypeIdParamName, newValue);
        }

        if (command.isChangeInStringParameterNamed(ClientApiConstants.contactKeyParamName, this.contactKey)) {
            final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.contactKeyParamName);
            actualChanges.put(ClientApiConstants.contactKeyParamName, newValue);
            this.contactKey = StringUtils.defaultIfEmpty(newValue, null);
        }

        if (command.isChangeInBooleanParameterNamed(ClientApiConstants.currentContactParamName, this.current)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(ClientApiConstants.currentContactParamName);
            actualChanges.put(ClientApiConstants.currentContactParamName, newValue);
            this.current = newValue;
        }

        final String statusName = command.stringValueOfParameterNamed(ClientApiConstants.statusParamName);
        ClientIdentifierStatus status = ClientIdentifierStatus.valueOf(statusName.toUpperCase());
        if (!status.getValue().equals(ClientIdentifierStatus.fromInt(this.status).getValue())) {
            final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.statusParamName);
            actualChanges.put(ClientApiConstants.statusParamName, ClientIdentifierStatus.valueOf(newValue));
            this.status = ClientIdentifierStatus.valueOf(newValue).getValue();
        }

        return actualChanges;
    }
}
