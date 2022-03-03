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
package org.apache.fineract.portfolio.client.data;

import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object represent client identity data.
 */
public class ClientIdentifierData {

    private final Long id;
    private final Long clientId;
    private final CodeValueData documentType;
    private final String documentIssueCountry;
    private final String documentKey;
    private final String description;
    private final String status;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> allowedDocumentTypes;
    private final Collection<CodeData> documentTypeData;

    public static ClientIdentifierData singleItem(final Long id, final Long clientId, final CodeValueData documentType,
            final String documentIssueCountry, final String documentKey, final String status, final String description) {
        return new ClientIdentifierData(id, clientId, documentType, null, documentIssueCountry, documentKey, description, status, null);
    }

    public static ClientIdentifierData documentTypeData(final Collection<CodeData> documentTypeData) {
        return new ClientIdentifierData(null, null, null, documentTypeData, null, null, null, null, null);
    }

    public static ClientIdentifierData template(final Collection<CodeValueData> codeValues) {
        return new ClientIdentifierData(null, null, null, null, null, null, null, null, codeValues);
    }

    public static ClientIdentifierData template(final ClientIdentifierData data, final Collection<CodeValueData> codeValues) {
        return new ClientIdentifierData(data.id, data.clientId, data.documentType, data.documentTypeData, data.documentIssueCountry,
                data.documentKey, data.description, data.status, codeValues);
    }

    public ClientIdentifierData(final Long id, final Long clientId, final CodeValueData documentType,
            final Collection<CodeData> documentTypeData, final String documentIssueCountry, final String documentKey,
            final String description, final String status, final Collection<CodeValueData> allowedDocumentTypes) {
        this.id = id;

        this.clientId = clientId;
        this.documentType = documentType;
        this.documentTypeData = documentTypeData;
        this.documentIssueCountry = documentIssueCountry;
        this.documentKey = documentKey;
        this.description = description;
        this.allowedDocumentTypes = allowedDocumentTypes;
        this.status = status;
    }
}
