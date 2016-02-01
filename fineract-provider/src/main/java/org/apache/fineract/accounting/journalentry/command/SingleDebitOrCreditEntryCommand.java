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
package org.apache.fineract.accounting.journalentry.command;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Immutable command for adding a "Credit" entry to the Journal
 */
public class SingleDebitOrCreditEntryCommand {

    private final Long glAccountId;
    private final BigDecimal amount;
    private final String comments;

    private final Set<String> parametersPassedInRequest;

    public SingleDebitOrCreditEntryCommand(final Set<String> parametersPassedInRequest, final Long glAccountId, final BigDecimal amount,
            final String comments) {
        this.parametersPassedInRequest = parametersPassedInRequest;
        this.glAccountId = glAccountId;
        this.amount = amount;
        this.comments = comments;
    }

    public boolean isGlAccountIdChanged() {
        return this.parametersPassedInRequest.contains("glAccountId");
    }

    public boolean isGlAmountChanged() {
        return this.parametersPassedInRequest.contains("amount");
    }

    public boolean isCommentsChanged() {
        return this.parametersPassedInRequest.contains("comments");
    }

    public Long getGlAccountId() {
        return this.glAccountId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public String getComments() {
        return this.comments;
    }

    public Set<String> getParametersPassedInRequest() {
        return this.parametersPassedInRequest;
    }

}