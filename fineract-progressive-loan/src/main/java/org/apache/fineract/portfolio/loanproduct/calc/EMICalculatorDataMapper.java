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
package org.apache.fineract.portfolio.loanproduct.calc;

import java.util.List;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanproduct.calc.converter.LoanReAgeParameterConverter;
import org.apache.fineract.portfolio.loanproduct.calc.converter.LoanTransactionConverter;
import org.apache.fineract.portfolio.loanproduct.calc.converter.RepaymentScheduleInstallmentConverter;
import org.apache.fineract.portfolio.loanproduct.calc.data.LoanReAgeParameterData;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProcessedTransactionData;
import org.apache.fineract.portfolio.loanproduct.calc.data.RepaymentScheduleInstallmentData;

public final class EMICalculatorDataMapper {

    private EMICalculatorDataMapper() {}

    public static RepaymentScheduleInstallmentData toRepaymentScheduleInstallmentData(LoanRepaymentScheduleInstallment installment) {
        return RepaymentScheduleInstallmentConverter.toData(installment);
    }

    public static List<RepaymentScheduleInstallmentData> toRepaymentScheduleInstallmentDataList(
            List<LoanRepaymentScheduleInstallment> installments) {
        return RepaymentScheduleInstallmentConverter.toDataList(installments);
    }

    public static LoanReAgeParameterData toLoanReAgeParameterData(LoanReAgeParameter parameter) {
        return LoanReAgeParameterConverter.toData(parameter);
    }

    public static ProcessedTransactionData toProcessedTransactionData(LoanTransaction transaction) {
        return LoanTransactionConverter.toData(transaction);
    }

    public static List<ProcessedTransactionData> toProcessedTransactionDataList(List<LoanTransaction> transactions) {
        return LoanTransactionConverter.toDataList(transactions);
    }
}
