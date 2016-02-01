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

import org.joda.time.DateTime;

/**
 * Immutable data object representing client data.
 */
public final class AuditData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String actionName;
    private final String entityName;
    @SuppressWarnings("unused")
    private final Long resourceId;
    @SuppressWarnings("unused")
    private final Long subresourceId;
    @SuppressWarnings("unused")
    private final String maker;
    @SuppressWarnings("unused")
    private final DateTime madeOnDate;
    @SuppressWarnings("unused")
    private final String checker;
    @SuppressWarnings("unused")
    private final DateTime checkedOnDate;
    @SuppressWarnings("unused")
    private final String processingResult;
    private String commandAsJson;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final String groupLevelName;
    @SuppressWarnings("unused")
    private final String groupName;
    @SuppressWarnings("unused")
    private final String clientName;
    @SuppressWarnings("unused")
    private final String loanAccountNo;
    @SuppressWarnings("unused")
    private final String savingsAccountNo;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final String url;

    public AuditData(final Long id, final String actionName, final String entityName, final Long resourceId, final Long subresourceId,
            final String maker, final DateTime madeOnDate, final String checker, final DateTime checkedOnDate,
            final String processingResult, final String commandAsJson, final String officeName, final String groupLevelName,
            final String groupName, final String clientName, final String loanAccountNo, final String savingsAccountNo,
            final Long clientId, final Long loanId, final String url) {

        this.id = id;
        this.actionName = actionName;
        this.entityName = entityName;
        this.resourceId = resourceId;
        this.subresourceId = subresourceId;
        this.maker = maker;
        this.madeOnDate = madeOnDate;
        this.checker = checker;
        this.checkedOnDate = checkedOnDate;
        this.commandAsJson = commandAsJson;
        this.processingResult = processingResult;
        this.officeName = officeName;
        this.groupLevelName = groupLevelName;
        this.groupName = groupName;
        this.clientName = clientName;
        this.loanAccountNo = loanAccountNo;
        this.savingsAccountNo = savingsAccountNo;
        this.clientId = clientId;
        this.loanId = loanId;
        this.url = url;
    }

    public void setCommandAsJson(final String commandAsJson) {
        this.commandAsJson = commandAsJson;
    }

    public String getCommandAsJson() {
        return this.commandAsJson;
    }

    public String getEntityName() {
        return this.entityName;
    }
}