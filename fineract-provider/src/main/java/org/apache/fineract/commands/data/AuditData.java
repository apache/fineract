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
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object representing client data.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class AuditData {

    private Long id;
    private String actionName;
    private String entityName;
    private Long resourceId;
    private Long subresourceId;
    private String maker;
    private ZonedDateTime madeOnDate;
    private String checker;
    private ZonedDateTime checkedOnDate;
    private String processingResult;
    private String commandAsJson;
    private String officeName;
    private String groupLevelName;
    private String groupName;
    private String clientName;
    private String loanAccountNo;
    private String savingsAccountNo;
    private Long clientId;
    private Long loanId;
    private String url;
}
