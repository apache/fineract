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

import static org.apache.fineract.portfolio.savings.DepositsApiConstants.toSavingsAccountIdParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.transferDescriptionParamName;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.savings.DepositAccountOnClosureType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

public class FixedDepositPreClosureReq {

    private Locale locale;
    private String dateFormat;
    private LocalDate closedDate;
    private DepositAccountOnClosureType closureType;
    private DateTimeFormatter formatter;
    private Long toSavingsAccountId;
    private SavingsAccount linkedSavingsAccount;
    private String transferDescription;
    private boolean topUp;

    public static FixedDepositPreClosureReq instance(JsonCommand command) {
        FixedDepositPreClosureReq instance = new FixedDepositPreClosureReq();
        Integer onAccountClosureId = command.integerValueOfParameterNamed(DepositsApiConstants.onAccountClosureIdParamName);
        instance.toSavingsAccountId = command.longValueOfParameterNamed(toSavingsAccountIdParamName);
        instance.transferDescription = command.stringValueOfParameterNamed(transferDescriptionParamName);
        instance.locale = command.extractLocale();
        instance.dateFormat = command.dateFormat();
        instance.formatter = DateTimeFormatter.ofPattern(instance.dateFormat).withLocale(instance.locale);
        instance.closedDate = command.localDateValueOfParameterNamed(SavingsApiConstants.closedOnDateParamName);
        instance.closureType = DepositAccountOnClosureType.fromInt(onAccountClosureId);
        return instance;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public LocalDate getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(LocalDate closedDate) {
        this.closedDate = closedDate;
    }

    public DepositAccountOnClosureType getClosureType() {
        return closureType;
    }

    public void setClosureType(DepositAccountOnClosureType closureType) {
        this.closureType = closureType;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    public Long getToSavingsAccountId() {
        return toSavingsAccountId;
    }

    public void setToSavingsAccountId(Long toSavingsAccountId) {
        this.toSavingsAccountId = toSavingsAccountId;
    }

    public SavingsAccount getLinkedSavingsAccount() {
        return linkedSavingsAccount;
    }

    public void setLinkedSavingsAccount(SavingsAccount linkedSavingsAccount) {
        this.linkedSavingsAccount = linkedSavingsAccount;
    }

    public String getTransferDescription() {
        return transferDescription;
    }

    public void setTransferDescription(String transferDescription) {
        this.transferDescription = transferDescription;
    }

    public boolean isTopUp() {
        return topUp;
    }

    public void setTopUp(boolean topUp) {
        this.topUp = topUp;
    }
}
