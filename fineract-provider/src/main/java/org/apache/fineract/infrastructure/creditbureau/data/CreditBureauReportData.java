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

package org.apache.fineract.infrastructure.creditbureau.data;

import java.io.Serializable;

public final class CreditBureauReportData implements Serializable {

    @SuppressWarnings("unused")
    private final String name;

    private final String gender;

    private final String address;

    private final String creditScore;

    private final String borrowerInfo;

    private final String[] openAccounts;

    private final String[] closedAccounts;

    public static CreditBureauReportData instance(final String name, final String gender, final String address, final String creditScore,
            final String borrowerInfo, final String[] openAccounts, final String[] closedAccounts) {
        return new CreditBureauReportData(name, gender, address, creditScore, borrowerInfo, openAccounts, closedAccounts);
    }

    public CreditBureauReportData(final String name, final String gender, final String address, final String creditScore,
            final String borrowerInfo, final String[] openAccounts, final String[] closedAccounts) {
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.creditScore = creditScore;
        this.borrowerInfo = borrowerInfo;
        this.openAccounts = openAccounts;
        this.closedAccounts = closedAccounts;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getCreditScore() {
        return creditScore;
    }

    public String getBorrowerInfo() {
        return borrowerInfo;
    }

    public String[] getOpenAccounts() {
        return openAccounts;
    }

    public String[] getClosedAccounts() {
        return closedAccounts;
    }
}
