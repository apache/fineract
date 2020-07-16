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
package org.apache.fineract.commands.data;

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FineractEventData {

    private final CommandWrapper request;
    private final CommandProcessingResult response;
    private final String tenantIdentifier;
    private final String timestamp;
    private final UUID contextId;
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public FineractEventData(CommandWrapper request, CommandProcessingResult response, String tenantIdentifier) {

        this.request = request;
        this.response = response;
        this.tenantIdentifier = tenantIdentifier;
        this.timestamp = df.format(new Date());
        this.contextId = UUID.randomUUID();
    }

    public CommandWrapper getRequest() {
        return request;
    }

    public CommandProcessingResult getResponse() {
        return response;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public UUID getContextId() {
        return contextId;
    }
}
