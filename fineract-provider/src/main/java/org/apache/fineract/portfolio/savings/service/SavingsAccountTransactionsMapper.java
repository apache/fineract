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
package org.apache.fineract.portfolio.savings.service;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY_DB_FIELD;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.springframework.jdbc.core.RowMapper;

public class SavingsAccountTransactionsMapper implements RowMapper<SavingsAccountTransactionData> {

    private static final String SELECT = buildSelect();
    private static final String FROM = buildFrom();
    private static final String SCHEMA = SELECT + FROM;

    public SavingsAccountTransactionsMapper() {}

    protected static String buildSelect() {
        return "tr.id as transactionId, tr.transaction_type_enum as transactionType, "
                + "tr.transaction_date as transactionDate, tr.transaction_time as transactionTime, tr.transaction_time_offset as transactionTimeOffset, tr.external_id as externalId, tr.amount as transactionAmount, "
                + "tr.release_id_of_hold_amount as releaseTransactionId, tr.reason_for_block as reasonForBlock, "
                + "tr.submitted_on_date as submittedOnDate, au.username as submittedByUsername, nt.note as transactionNote, "
                + "tr.running_balance_derived as runningBalance, tr.is_reversed as reversed, "
                + "tr.is_reversal as isReversal, tr.original_transaction_id as originalTransactionId, tr.is_lien_transaction as lienTransaction, "
                + "fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed, "
                + "fromtran.transaction_date as fromTransferDate, fromtran.transaction_time as fromTransferTime, fromtran.transaction_time_offset as fromTransferTimeOffset, fromtran.amount as fromTransferAmount, "
                + "fromtran.description as fromTransferDescription, "
                + "totran.id as toTransferId, totran.is_reversed as toTransferReversed, "
                + "totran.transaction_date as toTransferDate, totran.transaction_time as toTransferTime, totran.transaction_time_offset as toTransferTimeOffset, totran.amount as toTransferAmount, "
                + "totran.description as toTransferDescription, sa.id as savingsId, sa.account_no as accountNo, "
                + "pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, "
                + "pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, "
                + "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, "
                + "curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, "
                + "curr.display_symbol as currencyDisplaySymbol, pt.value as paymentTypeName, " + "tr.is_manual as postInterestAsOn ";
    }

    protected static String buildFrom() {
        return " FROM m_savings_account_transaction tr join m_savings_account sa on tr.savings_account_id = sa.id "
                + "join m_currency curr on curr.code = sa.currency_code "
                + "left join m_account_transfer_transaction fromtran on fromtran.from_savings_transaction_id = tr.id "
                + "left join m_account_transfer_transaction totran on totran.to_savings_transaction_id = tr.id "
                + "left join m_payment_detail pd on tr.payment_detail_id = pd.id "
                + "left join m_payment_type pt on pd.payment_type_id = pt.id left join m_appuser au on au.id= tr." + CREATED_BY_DB_FIELD
                + " left join m_note nt ON nt.savings_account_transaction_id=tr.id ";
    }

    public String schema() {
        return SCHEMA;
    }

    public String select() {
        return SELECT;
    }

    public String from() {
        return FROM;
    }

    @Override
    public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
        final Long id = rs.getLong("transactionId");
        final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

        final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
        final OffsetTime transactionTime = getOffsetTime(rs, "transactionTime", "transactionTimeOffset");
        final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
        final ExternalId externalId = ExternalIdFactory.produce(rs.getString("externalId"));
        final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
        final Long releaseTransactionId = rs.getLong("releaseTransactionId");
        final String reasonForBlock = rs.getString("reasonForBlock");
        final BigDecimal outstandingChargeAmount = null;
        final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
        final boolean reversed = rs.getBoolean("reversed");
        final boolean isReversal = rs.getBoolean("isReversal");
        final Long originalTransactionId = rs.getLong("originalTransactionId");
        final Boolean lienTransaction = rs.getBoolean("lienTransaction");

        final Long savingsId = rs.getLong("savingsId");
        final String accountNo = rs.getString("accountNo");
        final boolean postInterestAsOn = rs.getBoolean("postInterestAsOn");

        PaymentDetailData paymentDetailData = null;
        if (transactionType.isDepositOrWithdrawal()) {
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
            if (paymentTypeId != null) {
                final String typeName = rs.getString("paymentTypeName");
                final PaymentTypeData paymentType = PaymentTypeData.builder().id(paymentTypeId).name(typeName).build();
                final String accountNumber = rs.getString("accountNumber");
                final String checkNumber = rs.getString("checkNumber");
                final String routingCode = rs.getString("routingCode");
                final String receiptNumber = rs.getString("receiptNumber");
                final String bankNumber = rs.getString("bankNumber");
                paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                        bankNumber);
            }
        }

        final String currencyCode = rs.getString("currencyCode");
        final String currencyName = rs.getString("currencyName");
        final String currencyNameCode = rs.getString("currencyNameCode");
        final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
        final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);

        AccountTransferData transfer = null;
        final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
        final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
        if (fromTransferId != null) {
            final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
            final OffsetTime fromTransferTime = getOffsetTime(rs, "fromTransferTime", "fromTransferTimeOffset");
            final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
            final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
            final String fromTransferDescription = rs.getString("fromTransferDescription");

            transfer = AccountTransferData.transferBasicDetails(fromTransferId, currency, fromTransferAmount, fromTransferDate,
                    fromTransferTime, fromTransferDescription, fromTransferReversed);
        } else if (toTransferId != null) {
            final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
            final OffsetTime toTransferTime = getOffsetTime(rs, "toTransferTime", "toTransferTimeOffset");
            final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
            final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
            final String toTransferDescription = rs.getString("toTransferDescription");

            transfer = AccountTransferData.transferBasicDetails(toTransferId, currency, toTransferAmount, toTransferDate, toTransferTime,
                    toTransferDescription, toTransferReversed);
        }
        final String submittedByUsername = rs.getString("submittedByUsername");
        final String note = rs.getString("transactionNote");
        return SavingsAccountTransactionData
                .create(id, transactionType, paymentDetailData, savingsId, accountNo, externalId, date, currency, amount,
                        outstandingChargeAmount, runningBalance, reversed, transfer, submittedOnDate, postInterestAsOn, submittedByUsername,
                        note, isReversal, originalTransactionId, lienTransaction, releaseTransactionId, reasonForBlock)
                .withTransactionTime(transactionTime);
    }

    private static OffsetTime getOffsetTime(final ResultSet rs, final String timeColumn, final String offsetColumn) throws SQLException {
        final LocalTime time = JdbcSupport.getLocalTime(rs, timeColumn);
        final Integer offset = JdbcSupport.getInteger(rs, offsetColumn);
        return time == null ? null : OffsetTime.of(time, ZoneOffset.ofTotalSeconds(offset));
    }
}
