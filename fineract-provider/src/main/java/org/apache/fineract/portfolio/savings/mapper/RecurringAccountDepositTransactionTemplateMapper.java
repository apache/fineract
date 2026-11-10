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
package org.apache.fineract.portfolio.savings.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.springframework.jdbc.core.RowMapper;

public final class RecurringAccountDepositTransactionTemplateMapper implements RowMapper<SavingsAccountTransactionData> {

    public static final String ID = "id";
    public static final String ACCOUNT_NO = "accountNo";
    public static final String DUEAMOUNT = "dueamount";
    public static final String OUTSTANDING_CHARGE_AMOUNT = "outstandingChargeAmount";
    public static final String DUEDATE = "duedate";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String CURRENCY_NAME = "currencyName";
    public static final String CURRENCY_NAME_CODE = "currencyNameCode";
    public static final String CURRENCY_DISPLAY_SYMBOL = "currencyDisplaySymbol";
    public static final String CURRENCY_DIGITS = "currencyDigits";
    public static final String IN_MULTIPLES_OF = "inMultiplesOf";
    public static final String RUNNING_BALANCE = "runningBalance";
    private final String schemaSql;

    public RecurringAccountDepositTransactionTemplateMapper() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
        sqlBuilder.append(
                "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
        sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
        sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
        sqlBuilder.append("sa.account_balance_derived as runningBalance, ");
        sqlBuilder.append("mss.duedate as duedate, (mss.deposit_amount - coalesce(mss.deposit_amount_completed_derived,0)) as dueamount, ");
        sqlBuilder.append("coalesce(sac.amount_outstanding_derived,0.0) AS outstandingChargeAmount ");
        sqlBuilder.append("from m_savings_account sa ");
        sqlBuilder.append("join m_mandatory_savings_schedule mss  on mss.savings_account_id=sa.id and mss.completed_derived = false ");
        sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
        sqlBuilder.append("LEFT JOIN(SELECT s.savings_account_id AS savings_account_id ");
        sqlBuilder.append(",SUM(COALESCE(s.amount_outstanding_derived,0.0)) AS amount_outstanding_derived  ");
        sqlBuilder.append("FROM m_savings_account_charge s  ");
        sqlBuilder.append("JOIN m_charge c ON c.id = s.charge_id AND c.charge_time_enum = 3 ");
        sqlBuilder.append("WHERE s.savings_account_id = ? ");
        sqlBuilder.append("AND s.is_active = TRUE GROUP BY s.savings_account_id)sac ON sac.savings_account_id = sa.id ");

        this.schemaSql = sqlBuilder.toString();
    }

    public String schema() {
        return this.schemaSql;
    }

    @Override
    public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long savingsId = rs.getLong(ID);
        final String accountNo = rs.getString(ACCOUNT_NO);
        final BigDecimal dueamount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, DUEAMOUNT);
        final BigDecimal outstandingChargeAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, OUTSTANDING_CHARGE_AMOUNT);
        final LocalDate duedate = JdbcSupport.getLocalDate(rs, DUEDATE);
        final String currencyCode = rs.getString(CURRENCY_CODE);
        final String currencyName = rs.getString(CURRENCY_NAME);
        final String currencyNameCode = rs.getString(CURRENCY_NAME_CODE);
        final String currencyDisplaySymbol = rs.getString(CURRENCY_DISPLAY_SYMBOL);
        final Integer currencyDigits = JdbcSupport.getInteger(rs, CURRENCY_DIGITS);
        final Integer inMultiplesOf = JdbcSupport.getInteger(rs, IN_MULTIPLES_OF);
        final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                currencyNameCode);
        final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations
                .transactionType(SavingsAccountTransactionType.DEPOSIT.getValue());
        final PaymentDetailData paymentDetailData = null;
        final AccountTransferData transfer = null;
        final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, RUNNING_BALANCE);
        final boolean postInterestAsOn = false;
        final String submittedByUsername = null;
        final String note = null;
        final LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
        return SavingsAccountTransactionData.create(savingsId, transactionType, paymentDetailData, savingsId, accountNo, duedate, currency,
                dueamount, outstandingChargeAmount, runningBalance, false, transfer, postInterestAsOn, submittedByUsername, note,
                submittedOnDate);
    }
}
