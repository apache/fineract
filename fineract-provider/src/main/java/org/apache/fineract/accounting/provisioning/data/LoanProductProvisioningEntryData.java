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
package org.apache.fineract.accounting.provisioning.data;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LoanProductProvisioningEntryData {

    private Long historyId;
    private Long officeId;
    private String officeName;
    private String currencyCode;
    private Long productId;
    private String productName;
    private Long categoryId;
    private String categoryName;
    private Long overdueInDays;
    private BigDecimal percentage;
    private BigDecimal balance;
    private BigDecimal amountreserved;
    private Long liablityAccount;
    private String liabilityAccountCode;
    private String liabilityAccountName;
    private Long expenseAccount;
    private String expenseAccountCode;
    private String expenseAccountName;
    private Long criteriaId;

}
