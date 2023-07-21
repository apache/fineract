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

import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * A {@link RuntimeException} thrown when client resources are not found.
 */
public class ClientNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ClientNotFoundException(final Long id) {
        super("error.msg.client.id.invalid", "Client with identifier " + id + " does not exist", id);
    }

    public ClientNotFoundException(final ExternalId externalId) {
        super("error.msg.client.id.invalid", "Client with identifier " + externalId + " does not exist", externalId);
    }

    public ClientNotFoundException() {
        super("error.msg.client.not.found.with.basic.details", "Client not found with basic details.");
    }

    public ClientNotFoundException(String value, String valueType) {
        super("error.msg.client.not.found.with." + valueType, "Client not found with valuer " + value + ".", value);
    }

    public ClientNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.client.id.invalid", "Client with identifier " + id + " does not exist", id, e);
    }
}
