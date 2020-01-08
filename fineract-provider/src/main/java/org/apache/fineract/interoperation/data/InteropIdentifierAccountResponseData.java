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
package org.apache.fineract.interoperation.data;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

public class InteropIdentifierAccountResponseData extends CommandProcessingResult {

    @NotEmpty
    private String accountId;


    protected InteropIdentifierAccountResponseData(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly, @NotNull String accountId) {
        super(resourceId, officeId, commandId, changesOnly);
        this.accountId = accountId;
    }

    protected static InteropIdentifierAccountResponseData build(Long resourceId, Long officeId, Long commandId, Map<String, Object> changesOnly, @NotNull String accountId) {
        return new InteropIdentifierAccountResponseData(resourceId, officeId, commandId, changesOnly, accountId);
    }

    protected static InteropIdentifierAccountResponseData build(Long commandId, @NotNull String accountId) {
        return build(null, null, commandId, null, accountId);
    }

    public static InteropIdentifierAccountResponseData build(@NotNull String accountId) {
        return build(null, accountId);
    }

    @NotNull
    public String getAccountId() {
        return accountId;
    }

    protected void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
