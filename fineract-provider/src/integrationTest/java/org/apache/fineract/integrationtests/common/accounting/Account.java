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
package org.apache.fineract.integrationtests.common.accounting;

public class Account {

    public enum AccountType {
        ASSET("1"), INCOME("4"), EXPENSE("5"), LIABILITY("2"), EQUITY("3");

        private final String accountValue;

        AccountType(final String accountValue) {
            this.accountValue = accountValue;
        }

        @Override
        public String toString() {
            return this.accountValue;
        }
    }

    private final AccountType accountType;
    private final Integer accountID;

    public Account(final Integer accountID, final AccountType accountType) {
        this.accountID = accountID;
        this.accountType = accountType;
    }

    public AccountType getAccountType() {
        return this.accountType;
    }

    public Integer getAccountID() {
        return this.accountID;
    }
}
