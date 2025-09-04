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
package org.apache.fineract.portfolio.collectionsheet.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualClientData;
import org.apache.fineract.portfolio.collectionsheet.data.IndividualCollectionSheetLoanFlatData;
import org.apache.fineract.portfolio.collectionsheet.data.SavingsDueData;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CollectionSheetDao {

    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<IndividualCollectionSheetLoanFlatData> getIndividualCollectionSheetFlatDataList(LocalDate transactionDate,
            String officeHierarchy, Long officeId, Long staffId) {
        final boolean checkForOfficeId = officeId != null;
        final boolean checkForStaffId = staffId != null;
        final String transactionDateStr = DateUtils.DEFAULT_DATE_FORMATTER.format(transactionDate);

        final StringBuilder sqlString = getIndividualCollectionSheetFlatDataSql(checkForOfficeId, checkForStaffId);

        final SqlParameterSource namedParameters = getNamedParameters(transactionDateStr, officeHierarchy, officeId, staffId);

        return this.namedParameterJdbcTemplate.query(sqlString.toString(), namedParameters, rowMapper());
    }

    private SqlParameterSource getNamedParameters(String transactionDateStr, String officeHierarchy, Long officeId, Long staffId) {
        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", transactionDateStr)
                .addValue("officeHierarchy", officeHierarchy);

        final boolean checkForOfficeId = officeId != null;
        final boolean checkForStaffId = staffId != null;

        if (checkForOfficeId) {
            ((MapSqlParameterSource) namedParameters).addValue("officeId", officeId);
        }
        if (checkForStaffId) {
            ((MapSqlParameterSource) namedParameters).addValue("staffId", staffId);
        }
        return namedParameters;
    }

    private StringBuilder getIndividualCollectionSheetFlatDataSql(final boolean checkForOfficeId, final boolean checkforStaffId) {
        StringBuilder sqlString = new StringBuilder();
        sqlString.append("SELECT loandata.*, SUM(lc.amount_outstanding_derived) AS chargesDue ");
        sqlString.append("FROM (SELECT cl.display_name AS clientName, ");
        sqlString.append("cl.id AS clientId, ");
        sqlString.append("ln.id AS loanId, ");
        sqlString.append("ln.account_no AS accountId, ");
        sqlString.append("ln.loan_status_id AS accountStatusId, ");
        sqlString.append("pl.short_name AS productShortName, ");
        sqlString.append("ln.product_id AS productId, ");
        sqlString.append("ln.currency_code AS currencyCode, ");
        sqlString.append("ln.currency_digits AS currencyDigits, ");
        sqlString.append("ln.currency_multiplesof AS inMultiplesOf, ");
        sqlString.append("rc.");
        sqlString.append(sqlGenerator.escape("name"));
        sqlString.append(" AS currencyName, ");
        sqlString.append("rc.display_symbol AS currencyDisplaySymbol, ");
        sqlString.append("rc.internationalized_name_code AS currencyNameCode, ");
        sqlString.append("(CASE WHEN ln.loan_status_id = 200 THEN ln.principal_amount ELSE NULL END) AS " + "disbursementAmount, ");
        sqlString.append("SUM(COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls.principal_amount ELSE 0"
                + ".0 END), 0.0) - COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls"
                + ".principal_completed_derived ELSE 0.0 END), 0.0)) AS principalDue, ");
        sqlString.append("ln.principal_repaid_derived AS principalPaid, ");
        sqlString.append("SUM(COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls.interest_amount ELSE 0.0"
                + " END), 0.0) - COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls"
                + ".interest_completed_derived ELSE 0.0 END), 0.0)) AS interestDue, ");
        sqlString.append("ln.interest_repaid_derived AS interestPaid, ");
        sqlString.append("SUM(COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls.fee_charges_amount ELSE "
                + "0.0 END), 0.0) - COALESCE((CASE WHEN ln.loan_status_id = 300 THEN ls"
                + ".fee_charges_completed_derived ELSE 0.0 END), 0.0)) AS feeDue, ");
        sqlString.append("ln.fee_charges_repaid_derived AS feePaid ");
        sqlString.append("FROM m_loan ln ");
        sqlString.append("JOIN m_client cl ON cl.id = ln.client_id  ");
        sqlString.append("LEFT JOIN m_office ofc ON ofc.id = cl.office_id AND ofc.hierarchy LIKE " + ":officeHierarchy ");
        sqlString.append("LEFT JOIN m_product_loan pl ON pl.id = ln.product_id ");
        sqlString.append("LEFT JOIN m_currency rc ON rc.");
        sqlString.append(sqlGenerator.escape("code"));
        sqlString.append(" = ln.currency_code ");
        sqlString
                .append("JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate ");
        sqlString.append("WHERE ");
        if (checkForOfficeId) {
            sqlString.append("ofc.id = :officeId AND ");
        }
        if (checkforStaffId) {
            sqlString.append("ln.loan_officer_id = :staffId AND ");
        }
        sqlString.append("(ln.loan_status_id = 300) ");
        sqlString.append("AND ln.group_id IS NULL GROUP BY cl.id, ln.id ORDER BY cl.id, ln.id ) " + "loandata ");
        sqlString.append(
                "LEFT JOIN m_loan_charge lc ON lc.loan_id = loandata.loanId AND lc.is_paid_derived = false AND lc.is_active = true AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) ");
        sqlString.append("GROUP BY loandata.clientId, loandata.loanId ORDER BY loandata.clientId, loandata.loanId ");

        return sqlString;
    }

    private RowMapper<IndividualCollectionSheetLoanFlatData> rowMapper() {
        return (rs, rowNum) -> {
            final String clientName = rs.getString("clientName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final String accountId = rs.getString("accountId");
            final Integer accountStatusId = JdbcSupport.getInteger(rs, "accountStatusId");
            final String productShortName = rs.getString("productShortName");
            final Long productId = JdbcSupport.getLong(rs, "productId");
            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            CurrencyData currencyData = null;
            if (currencyCode != null) {
                currencyData = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                        currencyNameCode);
            }

            final BigDecimal disbursementAmount = rs.getBigDecimal("disbursementAmount");
            final BigDecimal principalDue = rs.getBigDecimal("principalDue");
            final BigDecimal principalPaid = rs.getBigDecimal("principalPaid");
            final BigDecimal interestDue = rs.getBigDecimal("interestDue");
            final BigDecimal interestPaid = rs.getBigDecimal("interestPaid");
            final BigDecimal chargesDue = rs.getBigDecimal("chargesDue");
            final BigDecimal feeDue = rs.getBigDecimal("feeDue");
            final BigDecimal feePaid = rs.getBigDecimal("feePaid");

            return new IndividualCollectionSheetLoanFlatData(clientName, clientId, loanId, accountId, accountStatusId, productShortName,
                    productId, currencyData, disbursementAmount, principalDue, principalPaid, interestDue, interestPaid, chargesDue, feeDue,
                    feePaid);
        };
    }

    public List<IndividualClientData> getIndividualClientData(String transactionDateStr, String officeHierarchy, Long officeId,
            Long staffId) {
        final boolean checkForOfficeId = officeId != null;
        final boolean checkForStaffId = staffId != null;

        final StringBuilder sqlString = getIndividualClientDataSql(checkForOfficeId, checkForStaffId);

        final SqlParameterSource namedParameters = getNamedParameters(transactionDateStr, officeHierarchy, officeId, staffId);

        return this.namedParameterJdbcTemplate.query(sqlString.toString(), namedParameters, individualClientDataExtractor());
    }

    private StringBuilder getIndividualClientDataSql(final boolean checkForOfficeId, final boolean checkForStaffId) {
        final StringBuilder sqlString = new StringBuilder(400);

        sqlString.append("SELECT (CASE WHEN sa.deposit_type_enum=100 THEN 'Saving Deposit' ELSE (CASE WHEN"
                + " sa.deposit_type_enum=300 THEN 'Recurring Deposit' ELSE 'Current "
                + "Deposit' END) END) AS depositAccountType, cl.display_name AS clientName," + " cl.id AS clientId, ");
        sqlString.append("sa.id AS savingsId, ");
        sqlString.append("sa.account_no AS accountId, ");
        sqlString.append("sa.status_enum AS accountStatusId, ");
        sqlString.append("sp.short_name AS productShortName, ");
        sqlString.append("sp.id AS productId, ");
        sqlString.append("sa.currency_code AS currencyCode, ");
        sqlString.append("sa.currency_digits AS currencyDigits, ");
        sqlString.append("sa.currency_multiplesof AS inMultiplesOf, ");
        sqlString.append("rc.");
        sqlString.append(sqlGenerator.escape("name"));
        sqlString.append(" AS currencyName, ");
        sqlString.append("rc.display_symbol AS currencyDisplaySymbol, ");
        sqlString.append("rc.internationalized_name_code AS currencyNameCode, ");
        sqlString.append("SUM(COALESCE(mss.deposit_amount,0) - coalesce(mss" + ".deposit_amount_completed_derived,0)) AS dueAmount ");
        sqlString.append("FROM m_savings_account sa ");
        sqlString.append("JOIN m_client cl ON cl.id = sa.client_id ");
        sqlString.append("JOIN m_savings_product sp ON sa.product_id = sp.id ");
        sqlString.append(
                "LEFT JOIN m_deposit_account_recurring_detail dard ON sa.id = dard.savings_account_id AND dard.is_mandatory = true AND dard.is_calendar_inherited = false ");
        sqlString.append(
                "LEFT JOIN m_mandatory_savings_schedule mss ON mss.savings_account_id=sa.id AND mss.completed_derived = 0 AND mss.duedate <= :dueDate ");
        sqlString.append("LEFT JOIN m_office ofc ON ofc.id = cl.office_id AND ofc.hierarchy like " + ":officeHierarchy ");
        sqlString.append("LEFT JOIN m_currency rc ON rc.");
        sqlString.append(sqlGenerator.escape("code"));
        sqlString.append(" = sa.currency_code ");
        sqlString.append("WHERE sa.status_enum=300 AND sa.group_id is null AND sa" + ".deposit_type_enum in (100,300,400) ");
        sqlString.append("AND (cl.status_enum = 300 OR (cl.status_enum = 600 AND cl.closedon_date" + " >= :dueDate)) ");
        if (checkForOfficeId) {
            sqlString.append("AND ofc.id = :officeId ");
        }
        if (checkForStaffId) {
            sqlString.append("AND sa.field_officer_id = :staffId ");
        }
        sqlString.append("GROUP BY cl.id, sa.id ORDER BY cl.id, sa.id ");
        return sqlString;
    }

    private ResultSetExtractor<List<IndividualClientData>> individualClientDataExtractor() {
        return rs -> {
            List<IndividualClientData> clientData = new ArrayList<>();
            int rowNum = 0;

            IndividualClientData client = null;
            Long previousClientId = null;

            while (rs.next()) {
                final Long clientId = JdbcSupport.getLong(rs, "clientId");

                // if we encounter a new client, create a fresh IndividualClientData
                if (previousClientId == null || !clientId.equals(previousClientId)) {
                    final String clientName = rs.getString("clientName");

                    client = IndividualClientData.instance(clientId, clientName);
                    client = IndividualClientData.withSavings(client, new ArrayList<SavingsDueData>());

                    clientData.add(client);
                    previousClientId = clientId;
                }

                // map savings for this row and attach to current client
                SavingsDueData saving = savingsDueDataRowMapper().mapRow(rs, rowNum);
                client.addSavings(saving);

                rowNum++;
            }

            return clientData;
        };
    }

    private RowMapper<SavingsDueData> savingsDueDataRowMapper() {
        return (rs, rowNum) -> {
            final Long savingsId = rs.getLong("savingsId");
            final String accountId = rs.getString("accountId");
            final Integer accountStatusId = JdbcSupport.getInteger(rs, "accountStatusId");
            final String productName = rs.getString("productShortName");
            final Long productId = rs.getLong("productId");
            final BigDecimal dueAmount = rs.getBigDecimal("dueAmount");

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");

            final String depositAccountType = rs.getString("depositAccountType");

            // build CurrencyData
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            return SavingsDueData.instance(savingsId, accountId, accountStatusId, productName, productId, currency, dueAmount,
                    depositAccountType);
        };
    }
}
