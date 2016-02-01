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
package org.apache.fineract.portfolio.savings.data;

import org.joda.time.LocalDate;

public class SavingsAccountAnnualFeeData {

    private final Long id;
    private final Long accountId;
    private final String accountNo;
    private final LocalDate nextAnnualFeeDueDate;

    public static SavingsAccountAnnualFeeData instance(final Long id, final Long accountId, final String accountNo,
            final LocalDate nextAnnualFeeDueDate) {
        return new SavingsAccountAnnualFeeData(id, accountId, accountNo, nextAnnualFeeDueDate);
    }

    private SavingsAccountAnnualFeeData(final Long id, final Long accountId, final String accountNo, final LocalDate nextAnnualFeeDueDate) {
        this.id = id;
        this.accountId = accountId;
        this.accountNo = accountNo;
        this.nextAnnualFeeDueDate = nextAnnualFeeDueDate;
    }

    public Long getId() {
        return this.id;
    }

    public Long getAccountId() {
        return this.accountId;
    }

    public LocalDate getNextAnnualFeeDueDate() {
        return this.nextAnnualFeeDueDate;
    }

    public String getAccountNo() {
        return this.accountNo;
    }
}