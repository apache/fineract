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

import java.time.LocalDate;

public final class SavingsApproval {

    private final transient Integer rowIndex;

    private final LocalDate approvedOnDate;

    private final String dateFormat;

    private final String locale;

    private final String note;

    public static SavingsApproval importInstance(LocalDate approvedOnDate, Integer rowIndex, String locale, String dateFormat) {
        return new SavingsApproval(approvedOnDate, rowIndex, locale, dateFormat);
    }

    private SavingsApproval(LocalDate approvedOnDate, Integer rowIndex, String locale, String dateFormat) {
        this.approvedOnDate = approvedOnDate;
        this.rowIndex = rowIndex;
        this.dateFormat = dateFormat;
        this.locale = locale;
        this.note = "";
    }

    public LocalDate getApprovedOnDate() {
        return approvedOnDate;
    }

    public String getLocale() {
        return locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public String getNote() {
        return note;
    }
}
