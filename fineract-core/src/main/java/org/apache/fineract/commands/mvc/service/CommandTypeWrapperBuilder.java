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
package org.apache.fineract.commands.mvc.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.mvc.domain.CommandTypeWrapper;

public class CommandTypeWrapperBuilder<T> {

    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long savingsId;
    private String actionName;
    private String entityName;
    private Long entityId;
    private Long subentityId;
    private String href;
    private T request;
    private CommandWrapper jsonWrapper;
    private String transactionId;
    private Long productId;
    private Long templateId;
    private Long creditBureauId;
    private Long organisationCreditBureauId;
    private String jobName;
    private String idempotencyKey;

    public CommandTypeWrapperBuilder() {}

    @SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "TODO: fix this!")
    public CommandTypeWrapper<T> build() {
        return new CommandTypeWrapper<>(this.officeId, this.groupId, this.clientId, this.loanId, this.savingsId, this.actionName,
                this.entityName, this.entityId, this.subentityId, this.href, this.request, this.transactionId, this.productId,
                this.templateId, this.creditBureauId, this.organisationCreditBureauId, this.jobName, this.idempotencyKey)
                .setJsonWrapper(this.jsonWrapper); // TODO: Remove after MVC migration
    }

    public CommandTypeWrapperBuilder<T> createFund() {
        this.actionName = "CREATE";
        this.entityName = "FUND";
        this.entityId = null;
        this.href = "/funds/template";
        return this;
    }

    public CommandTypeWrapperBuilder<T> updateFund(final Long fundId) {
        this.actionName = "UPDATE";
        this.entityName = "FUND";
        this.entityId = fundId;
        this.href = "/funds/" + fundId;
        return this;
    }

    public CommandTypeWrapperBuilder<T> withRequest(final T request) {
        this.request = request;
        return this;
    }

    // TODO: This method is temporarily added to support the use of old jsonCommand during the migration process from
    // jsonCommand to typeCommand.
    // Some logic still uses jsonCommand. Once the migration is complete and all logic has been updated to use
    // typeCommand, this method should be removed.
    public CommandTypeWrapperBuilder<T> withJsonCommand(final CommandWrapper jsonWrapper) {
        this.jsonWrapper = jsonWrapper;
        return this;
    }
}
