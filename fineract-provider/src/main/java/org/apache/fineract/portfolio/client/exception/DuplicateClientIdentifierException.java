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
package org.apache.fineract.portfolio.client.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a client identifier of the particular
 * type is already present
 */
public class DuplicateClientIdentifierException extends AbstractPlatformDomainRuleException {

    private Long documentTypeId;
    private String identifierKey;
    private final String identifierType;

    public DuplicateClientIdentifierException(final String identifierType) {
        super("error.msg.clientIdentifier.type.duplicate", "Active Client identifier of type " + identifierType
                + " is already present for this client", identifierType);
        this.identifierType = identifierType;
    }

    public DuplicateClientIdentifierException(final Long documentTypeId, final String identifierType, final String identifierKey) {
        super("error.msg.clientIdentifier.identityKey.duplicate", "Client identifier of type " + identifierType + " with value of "
                + identifierKey + " already exists.", identifierType, identifierKey);
        this.documentTypeId = documentTypeId;
        this.identifierType = identifierType;
        this.identifierKey = identifierKey;
    }

    public DuplicateClientIdentifierException(final String clientName, final String officeName, final String identifierType,
            final String identifierKey) {
        super("error.msg.clientIdentifier.identityKey.duplicate", "Client " + clientName + "under " + officeName + " Branch already has a "
                + identifierType + " with unique key " + identifierKey, clientName, officeName, identifierType, identifierKey);
        this.identifierType = identifierType;
        this.identifierKey = identifierKey;
    }

    public Long getDocumentTypeId() {
        return this.documentTypeId;
    }

    public String getIdentifierKey() {
        return this.identifierKey;
    }

    public String getIdentifierType() {
        return this.identifierType;
    }
}