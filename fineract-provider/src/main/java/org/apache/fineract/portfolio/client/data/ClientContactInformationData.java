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
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object represent client contact information data.
 */
@Getter
@Setter
public class ClientContactInformationData {

    private final Long id;
    private final Long clientId;
    private final CodeValueData contactType;
    private final String contactKey;
    private final String status;
    private final Boolean currentContact;
    private final Collection<CodeValueData> allowedContactTypes;

    public static ClientContactInformationData singleItem(final Long id, final Long clientId, final CodeValueData contactType,
            final String contactKey, final String status, final Boolean currentContact) {
        return new ClientContactInformationData(id, clientId, contactType, contactKey, status, currentContact, null);
    }

    public static ClientContactInformationData template(final Collection<CodeValueData> codeValues) {
        return new ClientContactInformationData(null, null, null, null, null, null, codeValues);
    }

    public static ClientContactInformationData template(final ClientContactInformationData data,
            final Collection<CodeValueData> codeValues) {
        return new ClientContactInformationData(data.id, data.clientId, data.contactType, data.contactKey, data.status, data.currentContact,
                codeValues);
    }

    public ClientContactInformationData(final Long id, final Long clientId, final CodeValueData contactType, final String contactKey,
            final String status, final Boolean currentContact, final Collection<CodeValueData> allowedContactTypes) {
        this.id = id;
        this.clientId = clientId;
        this.contactType = contactType;
        this.contactKey = contactKey;
        this.allowedContactTypes = allowedContactTypes;
        this.status = status;
        this.currentContact = currentContact;
    }
}
