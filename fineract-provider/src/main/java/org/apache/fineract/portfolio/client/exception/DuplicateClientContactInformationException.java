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
 * A {@link RuntimeException} thrown when a client contact information of the particular type is already present
 */
public class DuplicateClientContactInformationException extends AbstractPlatformDomainRuleException {

    public DuplicateClientContactInformationException(final String informationType) {
        super("error.msg.contactInformation.type.duplicate",
                "Active Client contact information of type " + informationType + " is already present for this client", informationType);
    }

    public DuplicateClientContactInformationException(final Long contactTypeId, final String informationType, final String informationKey) {
        super("error.msg.contactInformation.identityKey.duplicate",
                "Client contact information of type " + informationType + " with value of " + informationKey + " already exists.",
                informationType, informationKey);
    }

    public DuplicateClientContactInformationException(final String clientName, final String officeName, final String informationType,
            final String informationKey) {
        super("error.msg.contactInformation.identityKey.duplicate", "Client " + clientName + "under " + officeName
                + " Branch already has a " + informationType + " with unique key " + informationKey, clientName, officeName,
                informationType, informationKey);
    }
}
