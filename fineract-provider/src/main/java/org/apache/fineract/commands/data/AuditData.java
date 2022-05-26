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

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Immutable data object representing client data.
 */
@AllArgsConstructor
@Getter
public final class AuditData {

    private final Long id;
    private final String actionName;
    private final String entityName;
    private final Long resourceId;
    private final Long subresourceId;
    private final String maker;
    private final ZonedDateTime madeOnDate;
    private final String checker;
    private final ZonedDateTime checkedOnDate;
    private final String processingResult;
    @Setter
    private String commandAsJson;
    private final String officeName;
    private final String groupLevelName;
    private final String groupName;
    private final String clientName;
    private final String loanAccountNo;
    private final String savingsAccountNo;
    private final Long clientId;
    private final Long loanId;
    private final String url;
}
