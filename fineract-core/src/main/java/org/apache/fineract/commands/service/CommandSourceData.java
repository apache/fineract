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
package org.apache.fineract.commands.service;

import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandSourceData implements Serializable {

    private Long id;
    private String actionName;
    private String entityName;
    private Long officeId;
    private Long groupId;
    private Long clientId;
    private Long loanId;
    private Long savingsId;
    private String resourceGetUrl;
    private Long resourceId;
    private Long subResourceId;
    private String commandAsJson;
    private Long makerId;
    private OffsetDateTime madeOnDate;
    private OffsetDateTime checkedOnDate;
    private Long checkerId;
    private Integer status;
    private Long productId;
    private String transactionId;
    private Long creditBureauId;
    private Long organisationCreditBureauId;
    private String jobName;
    private String idempotencyKey;
    private String resourceExternalId;
    private String subResourceExternalId;
    private String result;
    private Integer resultStatusCode;
    private String loanExternalId;
    private boolean sanitized;
    private String batchId;

}
