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

package org.apache.fineract.portfolio.savings.request;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;

public class FixedDepositApprovalReq {

    private String note;
    private Locale locale;
    private String dateFormat;
    private LocalDate approvedOnDate;
    private String approvedOnDateChange;
    private DateTimeFormatter formatter;

    public static FixedDepositApprovalReq instance(JsonCommand command) {
        FixedDepositApprovalReq instance = new FixedDepositApprovalReq();
        instance.locale = command.extractLocale();
        instance.dateFormat = command.dateFormat();
        instance.note = command.stringValueOfParameterNamed("note");
        instance.formatter = DateTimeFormatter.ofPattern(instance.dateFormat).withLocale(instance.locale);
        instance.approvedOnDate = command.localDateValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);
        instance.approvedOnDateChange = command.stringValueOfParameterNamed(SavingsApiConstants.approvedOnDateParamName);
        return instance;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public LocalDate getApprovedOnDate() {
        return approvedOnDate;
    }

    public void setApprovedOnDate(LocalDate approvedOnDate) {
        this.approvedOnDate = approvedOnDate;
    }

    public String getApprovedOnDateChange() {
        return approvedOnDateChange;
    }

    public void setApprovedOnDateChange(String approvedOnDateChange) {
        this.approvedOnDateChange = approvedOnDateChange;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }
}
