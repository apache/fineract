/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_client_identifier", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "document_type_id", "document_key" }, name = "unique_identifier_key"),
        @UniqueConstraint(columnNames = { "client_id", "document_type_id" }, name = "unique_client_identifier") })
public class ClientIdentifier extends AbstractAuditableCustom<AppUser, Long> {

    @SuppressWarnings("unused")
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "document_type_id", nullable = false)
    private CodeValue documentType;

    @Column(name = "document_key", length = 1000)
    private String documentKey;

    @Column(name = "description", length = 1000)
    private String description;

    public static ClientIdentifier fromJson(final Client client, final CodeValue documentType, final JsonCommand command) {
        final String documentKey = command.stringValueOfParameterNamed("documentKey");
        final String description = command.stringValueOfParameterNamed("description");
        return new ClientIdentifier(client, documentType, documentKey, description);
    }

    protected ClientIdentifier() {
        //
    }

    private ClientIdentifier(final Client client, final CodeValue documentType, final String documentKey, final String description) {
        this.client = client;
        this.documentType = documentType;
        this.documentKey = StringUtils.defaultIfEmpty(documentKey, null);
        this.description = StringUtils.defaultIfEmpty(description, null);
    }

    public void update(final CodeValue documentType) {
        this.documentType = documentType;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String documentTypeIdParamName = "documentTypeId";
        if (command.isChangeInLongParameterNamed(documentTypeIdParamName, this.documentType.getId())) {
            final Long newValue = command.longValueOfParameterNamed(documentTypeIdParamName);
            actualChanges.put(documentTypeIdParamName, newValue);
        }

        final String documentKeyParamName = "documentKey";
        if (command.isChangeInStringParameterNamed(documentKeyParamName, this.documentKey)) {
            final String newValue = command.stringValueOfParameterNamed(documentKeyParamName);
            actualChanges.put(documentKeyParamName, newValue);
            this.documentKey = StringUtils.defaultIfEmpty(newValue, null);
        }

        final String descriptionParamName = "description";
        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }

        return actualChanges;
    }

    public String documentKey() {
        return this.documentKey;
    }

    public Long documentTypeId() {
        return this.documentType.getId();
    }
}