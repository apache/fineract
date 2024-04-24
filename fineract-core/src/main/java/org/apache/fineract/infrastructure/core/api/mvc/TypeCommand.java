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
package org.apache.fineract.infrastructure.core.api.mvc;

import java.util.Objects;
import org.apache.fineract.infrastructure.core.api.JsonCommand;

/**
 * Immutable representation of a command.
 *
 * Wraps the provided JSON with convenience functions for extracting parameter values and checking for changes against
 * an existing value.
 */
public final class TypeCommand<T> {

    private final T request;
    private JsonCommand jsonCommandObject; // TODO: Remove after MVC migration
    private final Long commandId;
    private final Long resourceId;
    private final Long subresourceId;
    private final Long groupId;
    private final Long clientId;
    private final Long loanId;
    private final Long savingsId;
    private final String entityName;
    private final String transactionId;
    private final String url;
    private final Long productId;
    private final Long creditBureauId;
    private final Long organisationCreditBureauId;
    private final String jobName;

    public static <T> TypeCommand<T> from(final T body, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId, final Long creditBureauId, final Long organisationCreditBureauId, final String jobName) {
        return new TypeCommand<>(null, body, entityName, resourceId, subresourceId, groupId, clientId, loanId, savingsId, transactionId,
                url, productId, creditBureauId, organisationCreditBureauId, jobName);

    }

    public TypeCommand(final Long commandId, final T request, final String entityName, final Long resourceId, final Long subresourceId,
            final Long groupId, final Long clientId, final Long loanId, final Long savingsId, final String transactionId, final String url,
            final Long productId, final Long creditBureauId, final Long organisationCreditBureauId, final String jobName) {

        this.commandId = commandId;
        this.request = request;
        this.entityName = entityName;
        this.resourceId = resourceId;
        this.subresourceId = subresourceId;
        this.groupId = groupId;
        this.clientId = clientId;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.transactionId = transactionId;
        this.url = url;
        this.productId = productId;
        this.creditBureauId = creditBureauId;
        this.organisationCreditBureauId = organisationCreditBureauId;
        this.jobName = jobName;
    }

    public Long getOrganisationCreditBureauId() {
        return this.organisationCreditBureauId;
    }

    public Long getCreditBureauId() {
        return this.creditBureauId;
    }

    public T getRequest() {
        return this.request;
    }

    public Long commandId() {
        return this.commandId;
    }

    public String entityName() {
        return this.entityName;
    }

    public Long entityId() {
        return this.resourceId;
    }

    public Long subentityId() {
        return this.subresourceId;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getSavingsId() {
        return this.savingsId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getUrl() {
        return this.url;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getJobName() {
        return this.jobName;
    }

    public boolean differenceExists(final String baseValue, final String workingCopyValue) {
        return !Objects.equals(baseValue, workingCopyValue);
    }

    // TODO: Remove after MVC migration
    public void setJsonCommand(JsonCommand jsonCommandObject) {
        this.jsonCommandObject = jsonCommandObject;
    }

    public JsonCommand getJsonCommand() {
        return Objects.requireNonNull(jsonCommandObject);
    }
}
