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
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.jdbc.core.RowMapper;

class LoanTransactionsMapper implements RowMapper<LoanTransactionData> {

    private final DatabaseSpecificSQLGenerator sqlGenerator;

    protected LoanTransactionsMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
    }

    public String loanPaymentsSchema() {

        return " tr.id as id, tr.transaction_type_enum as transactionType, tr.transaction_date as " + sqlGenerator.escape("date")
                + ", tr.amount as total, tr.principal_portion_derived as principal, tr.interest_portion_derived as interest, "
                + " tr.fee_charges_portion_derived as fees, tr.penalty_charges_portion_derived as penalties,  "
                + " tr.overpayment_portion_derived as overpayment, tr.outstanding_loan_balance_derived as outstandingLoanBalance, "
                + " tr.unrecognized_income_portion as unrecognizedIncome, tr.submitted_on_date as submittedOnDate, "
                + " tr.manually_adjusted_or_reversed as manuallyReversed, tr.reversal_external_id as reversalExternalId, tr.reversed_on_date as reversedOnDate, "
                + " pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, "
                + " pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, l.net_disbursal_amount as netDisbursalAmount,"
                + " l.currency_code as currencyCode, l.currency_digits as currencyDigits, l.currency_multiplesof as inMultiplesOf, rc."
                + sqlGenerator.escape("name") + " as currencyName, l.id as loanId, l.external_id as externalLoanId, "
                + " rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                + " pt.value as paymentTypeName, tr.external_id as externalId, tr.office_id as officeId, office.name as officeName, "
                + " fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,"
                + " fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,"
                + " fromtran.description as fromTransferDescription, "
                + " totran.id as toTransferId, totran.is_reversed as toTransferReversed, "
                + " totran.transaction_date as toTransferDate, totran.amount as toTransferAmount, "
                + " clcv.id as classificationCodeId, clcv.code_value as classificationCodeValue, "
                + " totran.description as toTransferDescription from m_loan l join m_loan_transaction tr on tr.loan_id = l.id "
                + " join m_currency rc on rc." + sqlGenerator.escape("code") + " = l.currency_code "
                + " left JOIN m_payment_detail pd ON tr.payment_detail_id = pd.id"
                + " left join m_payment_type pt on pd.payment_type_id = pt.id left join m_office office on office.id=tr.office_id"
                + " left join m_account_transfer_transaction fromtran on fromtran.from_loan_transaction_id = tr.id "
                + " left join m_account_transfer_transaction totran on totran.to_loan_transaction_id = tr.id "
                + " left join m_code_value clcv on clcv.id = tr.classification_cv_id ";
    }

    @Override
    public LoanTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        final Long id = rs.getLong("id");
        final Long loanId = rs.getLong("loanId");
        final String externalLoanIdStr = rs.getString("externalLoanId");
        final ExternalId externalLoanId = ExternalIdFactory.produce(externalLoanIdStr);
        final Long officeId = rs.getLong("officeId");
        final String officeName = rs.getString("officeName");
        final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(transactionTypeInt);
        final boolean manuallyReversed = rs.getBoolean("manuallyReversed");

        PaymentDetailData paymentDetailData = null;
        CodeValueData classificationData = null;
        Long classificationCodeValueId = JdbcSupport.getLong(rs, "classificationCodeId");
        if (classificationCodeValueId != null) {
            classificationData = CodeValueData.instance(classificationCodeValueId, rs.getString("classificationCodeValue"));
        }

        final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
        if (paymentTypeId != null) {
            final String typeName = rs.getString("paymentTypeName");
            final PaymentTypeData paymentType = PaymentTypeData.builder().id(paymentTypeId).name(typeName).build();
            final String accountNumber = rs.getString("accountNumber");
            final String checkNumber = rs.getString("checkNumber");
            final String routingCode = rs.getString("routingCode");
            final String receiptNumber = rs.getString("receiptNumber");
            final String bankNumber = rs.getString("bankNumber");
            paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber);
        }

        final LocalDate date = JdbcSupport.getLocalDate(rs, "date");
        final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
        final BigDecimal totalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "total");
        final BigDecimal principalPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
        final BigDecimal interestPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interest");
        final BigDecimal feeChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fees");
        final BigDecimal penaltyChargesPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penalties");
        final BigDecimal overPaymentPortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "overpayment");
        final BigDecimal unrecognizedIncomePortion = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "unrecognizedIncome");
        final BigDecimal outstandingLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "outstandingLoanBalance");
        final String externalIdStr = rs.getString("externalId");
        final ExternalId externalId = ExternalIdFactory.produce(externalIdStr);
        final String reversalExternalIdStr = rs.getString("reversalExternalId");
        final ExternalId reversalExternalId = ExternalIdFactory.produce(reversalExternalIdStr);
        final LocalDate reversedOnDate = JdbcSupport.getLocalDate(rs, "reversedOnDate");

        final BigDecimal netDisbursalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "netDisbursalAmount");

        AccountTransferData transfer = null;
        final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
        final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
        if (fromTransferId != null) {
            final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
            final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
            final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
            final String fromTransferDescription = rs.getString("fromTransferDescription");

            transfer = AccountTransferData.transferBasicDetails(fromTransferId, currencyData, fromTransferAmount, fromTransferDate,
                    fromTransferDescription, fromTransferReversed);
        } else if (toTransferId != null) {
            final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
            final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
            final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
            final String toTransferDescription = rs.getString("toTransferDescription");

            transfer = AccountTransferData.transferBasicDetails(toTransferId, currencyData, toTransferAmount, toTransferDate,
                    toTransferDescription, toTransferReversed);
        }

        return LoanTransactionData.builder().id(id).officeId(officeId).officeName(officeName).type(transactionType)
                .paymentDetailData(paymentDetailData).currency(currencyData).date(date).amount(totalAmount)
                .netDisbursalAmount(netDisbursalAmount).principalPortion(principalPortion).interestPortion(interestPortion)
                .feeChargesPortion(feeChargesPortion).penaltyChargesPortion(penaltyChargesPortion).overpaymentPortion(overPaymentPortion)
                .unrecognizedIncomePortion(unrecognizedIncomePortion).externalId(externalId).transfer(transfer)
                .outstandingLoanBalance(outstandingLoanBalance).submittedOnDate(submittedOnDate).manuallyReversed(manuallyReversed)
                .reversalExternalId(reversalExternalId).reversedOnDate(reversedOnDate).loanId(loanId).externalLoanId(externalLoanId)
                .classification(classificationData).build();
    }
}
