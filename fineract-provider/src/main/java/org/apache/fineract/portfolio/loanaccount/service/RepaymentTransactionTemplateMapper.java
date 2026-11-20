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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.springframework.jdbc.core.RowMapper;

final class RepaymentTransactionTemplateMapper implements RowMapper<LoanTransactionData> {

    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final CurrencyMapper currencyMapper = new CurrencyMapper();

    RepaymentTransactionTemplateMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public String schema() {
        // TODO: investigate whether it can be refactored to be more efficient
        final String principalDueExpression = "coalesce(ls.principal_amount, 0) - coalesce(ls.principal_writtenoff_derived, 0)"
                + " - coalesce(ls.principal_completed_derived, 0)";
        final String interestDueExpression = "coalesce(ls.interest_amount, 0) - coalesce(ls.interest_completed_derived, 0)"
                + " - coalesce(ls.interest_waived_derived, 0) - coalesce(ls.interest_writtenoff_derived, 0)";
        final String feeDueExpression = "coalesce(ls.fee_charges_amount, 0) - coalesce(ls.fee_charges_completed_derived, 0)"
                + " - coalesce(ls.fee_charges_writtenoff_derived, 0) - coalesce(ls.fee_charges_waived_derived, 0)";
        final String penaltyDueExpression = "coalesce(ls.penalty_charges_amount, 0)"
                + " - coalesce(ls.penalty_charges_completed_derived, 0) - coalesce(ls.penalty_charges_writtenoff_derived, 0)"
                + " - coalesce(ls.penalty_charges_waived_derived, 0)";
        final String totalDueExpression = principalDueExpression + " + " + interestDueExpression + " + " + feeDueExpression + " + "
                + penaltyDueExpression;
        return " GREATEST(loan_transaction.transaction_date, ls.dueDate) as transactionDate," + " " + principalDueExpression
                + " as principalDue," + " " + interestDueExpression + " as interestDue," + " " + feeDueExpression + " as feeDue," + " "
                + penaltyDueExpression + " as penaltyDue," + " l.currency_code as currencyCode," + " l.currency_digits as currencyDigits,"
                + " l.currency_multiplesof as inMultiplesOf," + " l.net_disbursal_amount as netDisbursalAmount," + " rc."
                + sqlGenerator.escape("name") + " as currencyName," + " rc.display_symbol as currencyDisplaySymbol,"
                + " rc.internationalized_name_code as currencyNameCode" + " FROM" + " m_loan l" + " JOIN m_currency rc on rc."
                + sqlGenerator.escape("code") + " = l.currency_code" + " JOIN m_loan_repayment_schedule ls ON ls.loan_id = l.id"
                + " LEFT JOIN (" + " select tr.loan_id, max(tr.transaction_date) as transaction_date" + " from m_loan_transaction tr"
                + " where tr.transaction_type_enum in (?,?)" + " AND tr.is_reversed = false" + " group by tr.loan_id"
                + " ) loan_transaction ON loan_transaction.loan_id = l.id" + " WHERE l.id = ?" + " ORDER BY CASE WHEN ("
                + totalDueExpression + ") > 0 THEN 0 ELSE 1 END, ls.installment" + " LIMIT 1";
    }

    @Override
    public LoanTransactionData mapRow(ResultSet rs, int rowNum) throws SQLException {
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.REPAYMENT);
        final CurrencyData currencyData = this.currencyMapper.mapRow(rs, rowNum);
        final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
        final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalDue");
        final BigDecimal interestDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestDue");
        final BigDecimal feeDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeDue");
        final BigDecimal penaltyDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyDue");
        final BigDecimal totalDue = principalPortion.add(interestDue).add(feeDue).add(penaltyDue);
        final BigDecimal outstandingLoanBalance = null;
        final BigDecimal unrecognizedIncomePortion = null;
        final BigDecimal overPaymentPortion = null;
        final BigDecimal netDisbursalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "netDisbursalAmount");
        final Long id = null;
        final Long loanId = null;
        final Long officeId = null;
        final String officeName = null;
        boolean manuallyReversed = false;
        final PaymentDetailData paymentDetailData = null;
        final AccountTransferData transfer = null;
        return LoanTransactionData.builder().id(id).officeId(officeId).officeName(officeName).type(transactionType)
                .paymentDetailData(paymentDetailData).currency(currencyData).date(date).amount(totalDue)
                .netDisbursalAmount(netDisbursalAmount).principalPortion(principalPortion).interestPortion(interestDue)
                .feeChargesPortion(feeDue).penaltyChargesPortion(penaltyDue).overpaymentPortion(overPaymentPortion)
                .externalId(ExternalId.empty()).transfer(transfer).outstandingLoanBalance(outstandingLoanBalance)
                .unrecognizedIncomePortion(unrecognizedIncomePortion).manuallyReversed(manuallyReversed).loanId(loanId)
                .externalLoanId(ExternalId.empty()).build();
    }

}
