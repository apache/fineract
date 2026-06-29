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

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A {@link RuntimeException} thrown when a client family member is not found.
 */
public class FamilyMemberNotFoundException extends AbstractPlatformResourceNotFoundException {

    public FamilyMemberNotFoundException(final Long familyMemberId, final Long clientId, final EmptyResultDataAccessException e) {
        super("error.msg.family.member.id.invalid",
                "Family member with identifier " + familyMemberId + " does not exist for client with identifier " + clientId,
                familyMemberId, clientId, e);
    }
}
