/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.staff.exception.StaffNotFoundException;
import org.mifosplatform.organisation.staff.service.StaffReadPlatformService;
import org.mifosplatform.organisation.teller.data.CashierData;
import org.mifosplatform.organisation.teller.data.CashierTransactionData;
import org.mifosplatform.organisation.teller.data.CashierTransactionTypeTotalsData;
import org.mifosplatform.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.mifosplatform.organisation.teller.data.TellerData;
import org.mifosplatform.organisation.teller.data.TellerJournalData;
import org.mifosplatform.organisation.teller.data.TellerTransactionData;
import org.mifosplatform.organisation.teller.domain.CashierTxnType;
import org.mifosplatform.organisation.teller.domain.TellerStatus;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class TellerManagementReadPlatformServiceImpl implements TellerManagementReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final TellerLookupMapper lookupMapper = new TellerLookupMapper();
    private final TellerInOfficeHierarchyMapper tellerInOfficeHierarchyMapper = new TellerInOfficeHierarchyMapper();
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public TellerManagementReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, StaffReadPlatformService staffReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
    }

    private static final class TellerMapper implements RowMapper<TellerData> {

        public String schema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append("t.id as id,t.office_id as office_id, t.name as teller_name, t.description as description, ");
            sqlBuilder.append("t.valid_from as start_date, t.valid_to as end_date, t.state as status, o.name as office_name, ");
            sqlBuilder.append("t.debit_account_id as debit_account_id, t.credit_account_id as credit_account_id ");
            sqlBuilder.append("from m_tellers t ");
            sqlBuilder.append("join m_office o on o.id = t.office_id ");

            return sqlBuilder.toString();
        }

        @Override
        public TellerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("office_id");
            final String tellerName = rs.getString("teller_name");
            final String description = rs.getString("description");
            final String officeName = rs.getString("office_name");
            TellerStatus tellerStatus = null;
            final Integer status = rs.getInt("status");
            if (status != null) {
                tellerStatus = TellerStatus.fromInt(status);
            }
            final Long debitAccountId = rs.getLong("debit_account_id");
            final Long creditAccountId = rs.getLong("credit_account_id");

            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "start_date");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "end_date");

            return TellerData.instance(id, officeId, debitAccountId, creditAccountId, tellerName, description, startDate, endDate,
                    tellerStatus, officeName, null, null);
        }
    }

    private static final class TellerInOfficeHierarchyMapper implements RowMapper<TellerData> {

        public String schema() {

            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append("t.id as id,t.office_id as office_id, t.name as teller_name, t.description as description, ");
            sqlBuilder.append("t.valid_from as start_date, t.valid_to as end_date, t.state as status, o.name as office_name ");
            sqlBuilder.append("t.debit_account_id as debit_account_id, t.credit_account_id as credit_account_id ");
            sqlBuilder.append("from m_office o ");
            sqlBuilder.append("join m_office ohierarchy on o.hierarchy like concat(ohierarchy.hierarchy, '%') ");
            sqlBuilder.append("join m_tellers t on t.office_id = ohierarchy.id and s.is_active=1 ");

            sqlBuilder.append("where o.id = ? ");

            return sqlBuilder.toString();
        }

        @Override
        public TellerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String tellerName = rs.getString("teller_name");
            final String description = rs.getString("description");
            final String officeName = rs.getString("office_name");
            final Long officeId = rs.getLong("office_id");
            TellerStatus tellerStatus = null;
            final Integer status = rs.getInt("status");
            if (status != null) {
                tellerStatus = TellerStatus.fromInt(status);
            }
            final Long debitAccountId = rs.getLong("debit_account_id");
            final Long creditAccountId = rs.getLong("credit_account_id");

            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "start_date");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "end_date");

            return TellerData.instance(id, officeId, debitAccountId, creditAccountId, tellerName, description, startDate, endDate,
                    tellerStatus, officeName, null, null);

        }
    }

    private static final class TellerLookupMapper implements RowMapper<TellerData> {

        private final String schemaSql;

        public TellerLookupMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(100);
            sqlBuilder.append("t.id as id, t.name as teller_name ");
            sqlBuilder.append("from m_tellers t ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public TellerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String tellerName = rs.getString("teller_name");
            return TellerData.lookup(id, tellerName);
        }
    }

    @Override
    public Collection<TellerData> retrieveAllTellersForDropdown(final Long officeId) {

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final String sql = "select " + this.lookupMapper.schema() + " where s.office_id = ? and s.is_active=1 ";

        return this.jdbcTemplate.query(sql, this.lookupMapper, new Object[] { defaultOfficeId });
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    public TellerData findTeller(final Long tellerId) {

        try {
            final TellerMapper tm = new TellerMapper();
            final String sql = "select " + tm.schema() + " where t.id = ?";

            return this.jdbcTemplate.queryForObject(sql, tm, new Object[] { tellerId });
        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(tellerId);
        }
    }

    @Override
    public Collection<TellerData> retrieveAllTellers(final String sqlSearch, final Long officeId, final String status) {
        final String extraCriteria = getTellerCriteria(sqlSearch, officeId, status);
        return retrieveAllTeller(extraCriteria);
    }

    private Collection<TellerData> retrieveAllTeller(final String extraCriteria) {

        final TellerMapper tm = new TellerMapper();
        String sql = "select " + tm.schema();
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " where " + extraCriteria;
        }
        sql = sql + " order by t.teller_name";
        return this.jdbcTemplate.query(sql, tm, new Object[] {});
    }

    private String getTellerCriteria(final String sqlSearch, final Long officeId, final String status) {

        final StringBuffer extraCriteria = new StringBuffer(200);

        if (sqlSearch != null) {
            extraCriteria.append(" and (").append(sqlSearch).append(")");
        }
        if (officeId != null) {
            extraCriteria.append(" and office_id = ").append(officeId).append(" ");
        }
        // Passing status parameter to get ACTIVE (By Default), INACTIVE or ALL
        // (Both active and Inactive) employees
        if (status.equalsIgnoreCase("active")) {
            extraCriteria.append(" and status = 300 ");
        } else if (status.equalsIgnoreCase("inActive")) {
            extraCriteria.append(" and status = 0 ");
        } else if (status.equalsIgnoreCase("all")) {} else {
            throw new UnrecognizedQueryParamException("status", status, new Object[] { "all", "active", "inactive" });
        }

        if (StringUtils.isNotBlank(extraCriteria.toString())) {
            extraCriteria.delete(0, 4);
        }

        // remove begin four letter including a space from the string.
        return extraCriteria.toString();
    }

    @Override
    public Collection<TellerData> getTellers(Long officeId) {
        return retrieveAllTellers(false);
    }

    @Override
    public Collection<CashierData> getCashiersForTeller(Long tellerId, Date fromDate, Date toDate) {
        return retrieveCashiersForTellers(null, tellerId);
    }

    @Override
    public Collection<CashierData> retrieveCashiersForTellers(final String sqlSearch, final Long tellerId) {
        final String extraCriteria = getTellerCriteria(sqlSearch, tellerId);
        return fetchCashiers(extraCriteria);
    }

    private String getTellerCriteria(final String sqlSearch, final Long tellerId) {

        final StringBuffer extraCriteria = new StringBuffer(200);

        if (sqlSearch != null) {
            extraCriteria.append(" and (").append(sqlSearch).append(")");
        }
        if (tellerId != null) {
            extraCriteria.append(" and teller_id = ").append(tellerId).append(" ");
        }

        // remove begin four letter including a space from the string.
        if (StringUtils.isNotBlank(extraCriteria.toString())) {
            extraCriteria.delete(0, 4);
        }

        return extraCriteria.toString();
    }

    private Collection<CashierData> fetchCashiers(final String extraCriteria) {

        final CashierMapper cm = new CashierMapper();
        String sql = "select " + cm.schema();
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " where " + extraCriteria;
        }
        sql = sql + " order by teller_name";
        return this.jdbcTemplate.query(sql, cm, new Object[] {});
    }

    @Override
    public CashierData findCashier(Long cashierId) {
        try {
            final CashierMapper cm = new CashierMapper();
            final String sql = "select " + cm.schema() + " where c.id = ?";

            return this.jdbcTemplate.queryForObject(sql, cm, new Object[] { cashierId });
        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(cashierId);
        }
    }

    @Override
    public Collection<CashierData> getCashierData(Long officeId, Long tellerId, Long staffId, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<CashierData> getTellerCashiers(Long tellerId, Date date) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TellerTransactionData findTellerTransaction(Long transactionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<TellerTransactionData> fetchTellerTransactionsByTellerId(Long tellerId, Date fromDate, Date toDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<TellerJournalData> getJournals(Long officeId, Long tellerId, Long cashierId, Date dateFrom, Date dateTo) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<TellerJournalData> fetchTellerJournals(Long tellerId, Long cashierId, Date fromDate, Date toDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Cacheable(value = "tellers", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')")
    public Collection<TellerData> retrieveAllTellers(final boolean includeAllTellers) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllTellers) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }
        final TellerMapper tm = new TellerMapper();
        final String sql = "select " + tm.schema() + "where o.hierarchy like ? order by o.hierarchy";

        return this.jdbcTemplate.query(sql, tm, new Object[] { hierarchySearchString });
    }

    @Override
    public CashierData retrieveCashierTemplate(Long officeId, Long tellerId, boolean staffInSelectedOfficeOnly) {
        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);

        final OfficeData officeData = this.officeReadPlatformService.retrieveOffice(defaultOfficeId);
        String officeName = "";
        if (officeData != null) {
            officeName = officeData.name();
        }

        TellerData tellerData = findTeller(tellerId);
        String tellerName = "";
        if (tellerData != null) {
            tellerName = tellerData.getName();
        }

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }

        return CashierData.template(officeId, officeName, tellerId, tellerName, staffOptions);
    }

    @Override
    public CashierTransactionData retrieveCashierTxnTemplate(Long cashierId) {
        String officeName = "";
        String tellerName = "";
        String cashierName = "";
        Long officeId = null;
        Long tellerId = null;
        Date startDate = null;
        Date endDate = null;

        CashierData cashierData = findCashier(cashierId);
        if (cashierData != null) {
            cashierName = cashierData.getStaffName();
            tellerId = cashierData.getTellerId();
            if (tellerId != null) {
                TellerData tellerData = findTeller(tellerId);
                if (tellerData != null) {
                    tellerName = tellerData.getName();
                    officeName = tellerData.getOfficeName();
                }
            }
            startDate = cashierData.getStartDate();
            endDate = cashierData.getEndDate();
        }
        // Fetching all currency type from m_organisation_currency table
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        return CashierTransactionData.template(cashierId, tellerId, tellerName, officeId, officeName, cashierName, cashierData, startDate,
                endDate, currencyOptions);
    }

    @Override
    public CashierTransactionsWithSummaryData retrieveCashierTransactionsWithSummary(final Long cashierId, final boolean includeAllTellers,
            final Date fromDate, final Date toDate) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllTellers) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }
        final CashierTransactionSummaryMapper ctsm = new CashierTransactionSummaryMapper();
        final String sql = "select " + ctsm.cashierTxnSummarySchema() + " limit 1000";

        Collection<CashierTransactionTypeTotalsData> cashierTxnTypeTotals = this.jdbcTemplate.query(sql, ctsm, new Object[] { cashierId,
                hierarchySearchString, cashierId, hierarchySearchString, cashierId, hierarchySearchString });

        Iterator<CashierTransactionTypeTotalsData> itr = cashierTxnTypeTotals.iterator();
        BigDecimal allocAmount = new BigDecimal(0);
        BigDecimal cashInAmount = new BigDecimal(0);
        BigDecimal cashOutAmount = new BigDecimal(0);
        BigDecimal settleAmount = new BigDecimal(0);

        while (itr.hasNext()) {
            CashierTransactionTypeTotalsData total = itr.next();
            if (total != null) {
                if (total.getCashierTxnType() == CashierTxnType.ALLOCATE.getId()) {
                    allocAmount = total.getCashTotal();
                } else if (total.getCashierTxnType() == CashierTxnType.SETTLE.getId()) {
                    settleAmount = total.getCashTotal();
                } else if (total.getCashierTxnType() == CashierTxnType.INWARD_CASH_TXN.getId()) {
                    cashInAmount = total.getCashTotal();
                } else if (total.getCashierTxnType() == CashierTxnType.OUTWARD_CASH_TXN.getId()) {
                    cashOutAmount = total.getCashTotal();
                }
            }
        }

        final Collection<CashierTransactionData> cashierTransactions = retrieveCashierTransactions(cashierId, includeAllTellers, fromDate,
                toDate);

        CashierTransactionData cashierTxnTemplate = retrieveCashierTxnTemplate(cashierId);

        CashierTransactionsWithSummaryData txnsWithSummary = CashierTransactionsWithSummaryData.instance(cashierTransactions, allocAmount,
                cashInAmount, cashOutAmount, settleAmount, cashierTxnTemplate.getOfficeName(), cashierTxnTemplate.getTellerId(),
                cashierTxnTemplate.getTellerName(), cashierTxnTemplate.getCashierId(), cashierTxnTemplate.getCashierName());
        return txnsWithSummary;
    }

    @Override
    public Collection<CashierTransactionData> retrieveCashierTransactions(final Long cashierId, final boolean includeAllTellers,
            final Date fromDate, final Date toDate) {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllTellers) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }

        final CashierTransactionMapper ctm = new CashierTransactionMapper();

        final String sql = "select * from (select " + ctm.cashierTxnSchema()
                + " where txn.cashier_id = ? and o.hierarchy like ? ) cashier_txns " + " union (select " + ctm.savingsTxnSchema()
                + " where sav_txn.is_reversed = 0 and c.id = ? and o.hierarchy like ? and "
                + " created_date between c.start_date and date_add(c.end_date, interval 1 day) "
                + " and renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge', 'withdrawal') ) " + " union (select "
                + ctm.loansTxnSchema() + " where loan_txn.is_reversed = 0 and c.id = ? and o.hierarchy like ? and "
                + " created_date between c.start_date and date_add(c.end_date, interval 1 day) "
                + " and renum.enum_value in ('Repayment At Disbursement','Repayment', 'Recovery Payment','Disbursement') ) "
                + " order by created_date ";

        return this.jdbcTemplate.query(sql, ctm, new Object[] { cashierId, hierarchySearchString, cashierId, hierarchySearchString,
                cashierId, hierarchySearchString });
    }

    private static final class CashierMapper implements RowMapper<CashierData> {

        public String schema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append("c.id as id,c.teller_id as teller_id, t.name as teller_name, c.description as description, ");
            sqlBuilder.append("c.staff_id as staff_id, s.display_name as staff_name,  ");
            sqlBuilder.append("c.start_date as start_date, c.end_date as end_date,  ");
            sqlBuilder.append("c.full_day as full_day, c.start_time as start_time, c.end_time as end_time ");
            sqlBuilder.append("from m_cashiers c ");
            sqlBuilder.append("join m_tellers t on t.id = c.teller_id ");
            sqlBuilder.append("join m_staff s on s.id = c.staff_id ");

            return sqlBuilder.toString();
        }

        @Override
        public CashierData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long tellerId = rs.getLong("teller_id");
            final String tellerName = rs.getString("teller_name");
            final Long staffId = rs.getLong("staff_id");
            final String staffName = rs.getString("staff_name");
            final String description = rs.getString("description");

            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "start_date");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "end_date");
            final Integer fullDayFromDB = rs.getInt("full_day");
            Boolean fullDay = false;
            if (fullDayFromDB == 1) {
                fullDay = true;
            }
            final String startTime = rs.getString("start_time");
            final String endTime = rs.getString("end_time");

            return CashierData.instance(id, null, null, staffId, staffName, tellerId, tellerName, description, startDate.toDate(),
                    endDate.toDate(), fullDay, startTime, endTime);
        }
    }

    private static final class CashierTransactionMapper implements RowMapper<CashierTransactionData> {

        public String cashierTxnSchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" txn.id as txn_id, txn.cashier_id as cashier_id, ");
            sqlBuilder.append(" txn.txn_type as txn_type, ");
            sqlBuilder.append(" txn.txn_amount as txn_amount, txn.txn_date as txn_date, txn.txn_note as txn_note, ");
            sqlBuilder.append(" txn.entity_type as entity_type, txn.entity_id as entity_id, txn.created_date as created_date, ");
            sqlBuilder
                    .append(" o.id as office_id, o.name as office_name, t.id as teller_id, t.name as teller_name, s.display_name as cashier_name ");
            sqlBuilder.append(" from m_cashier_transactions txn ");
            sqlBuilder.append(" left join m_cashiers c on c.id = txn.cashier_id ");
            sqlBuilder.append(" left join m_tellers t on t.id = c.teller_id ");
            sqlBuilder.append(" left join m_office o on o.id = t.office_id ");
            sqlBuilder.append(" left join m_staff s on s.id = c.staff_id ");

            return sqlBuilder.toString();
        }

        public String savingsTxnSchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" sav_txn.id as txn_id, null as cashier_id, ");
            sqlBuilder.append(" case ");
            sqlBuilder.append(" 	when renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge') ");
            sqlBuilder.append(" 		then 103 ");
            sqlBuilder.append(" 	when renum.enum_value in ('withdrawal', '') ");
            sqlBuilder.append(" 		then 104 ");
            sqlBuilder.append(" 	else ");
            sqlBuilder.append(" 		105 ");
            sqlBuilder.append(" end as txn_type, ");
            sqlBuilder.append(" sav_txn.amount as txn_amount, sav_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append(" concat (renum.enum_value, ', Sav:', sav.id, '-', sav.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append(" 'savings' as entity_type, sav.id as entity_id, sav_txn.created_date as created_date, ");
            sqlBuilder
                    .append(" o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append(" from m_savings_account_transaction sav_txn ");
            sqlBuilder
                    .append(" left join r_enum_value renum on sav_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'savings_transaction_type_enum' ");
            sqlBuilder.append(" left join m_savings_account sav on sav_txn.savings_account_id = sav.id ");
            sqlBuilder.append(" left join m_client cl on sav.client_id = cl.id ");
            sqlBuilder.append(" left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append(" left join m_appuser user on sav_txn.appuser_id = user.id ");
            sqlBuilder.append(" left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append(" left join m_cashiers c on c.staff_id = staff.id ");

            return sqlBuilder.toString();
        }

        public String loansTxnSchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" loan_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append(" case ");
            sqlBuilder.append(" 	when renum.enum_value in ('Repayment At Disbursement','Repayment', 'Recovery Payment') ");
            sqlBuilder.append(" 		then 103 ");
            sqlBuilder.append(" 	when renum.enum_value in ('Disbursement') ");
            sqlBuilder.append(" 		then 104 ");
            sqlBuilder.append(" 	else ");
            sqlBuilder.append(" 		105 ");
            sqlBuilder.append(" end as cash_txn_type, ");
            sqlBuilder.append(" loan_txn.amount as txn_amount, loan_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append(" concat (renum.enum_value, ', Loan:', loan.id, '-', loan.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append(" 'loans' as entity_type, loan.id as entity_id, loan_txn.created_date as created_date, ");
            sqlBuilder
                    .append(" o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append(" from m_loan_transaction loan_txn ");
            sqlBuilder
                    .append(" left join r_enum_value renum on loan_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'transaction_type_enum' ");
            sqlBuilder.append(" left join m_loan loan on loan_txn.loan_id = loan.id ");
            sqlBuilder.append(" left join m_client cl on loan.client_id = cl.id ");
            sqlBuilder.append(" left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append(" left join m_appuser user on loan_txn.appuser_id = user.id ");
            sqlBuilder.append(" left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append(" left join m_cashiers c on c.staff_id = staff.id ");

            return sqlBuilder.toString();
        }

        @Override
        public CashierTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("txn_id");
            final Long cashierId = rs.getLong("cashier_id");
            final Integer tType = rs.getInt("txn_type");
            final CashierTxnType txnType = CashierTxnType.getCashierTxnType(tType);
            final BigDecimal txnAmount = rs.getBigDecimal("txn_amount");
            final LocalDate txnLocalDate = JdbcSupport.getLocalDate(rs, "txn_date");
            final String txnNote = rs.getString("txn_note");
            final String entityType = rs.getString("entity_type");
            final Long entityId = rs.getLong("entity_id");
            final LocalDate createdLocalDate = JdbcSupport.getLocalDate(rs, "created_date");

            Date txnDate = null;
            if (txnLocalDate != null) {
                txnDate = txnLocalDate.toDate();
            }
            Date createdDate = null;
            if (createdLocalDate != null) {
                createdDate = createdLocalDate.toDate();
            }

            final Long officeId = rs.getLong("office_id");
            final String officeName = rs.getString("office_name");
            final Long tellerId = rs.getLong("teller_id");
            final String tellerName = rs.getString("teller_name");
            final String cashierName = rs.getString("cashier_name");

            return CashierTransactionData.instance(id, cashierId, txnType, txnAmount, txnDate, txnNote, entityType, entityId, createdDate,
                    officeId, officeName, tellerId, tellerName, cashierName, null, null, null);
        }
    }

    private static final class CashierTransactionSummaryMapper implements RowMapper<CashierTransactionTypeTotalsData> {

        public String cashierTxnSummarySchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" cash_txn_type, sum(txn_amount) as txn_total from ");
            sqlBuilder.append(" (select * from ");
            sqlBuilder.append(" (select txn.id as txn_id, txn.cashier_id as cashier_id, ");
            sqlBuilder.append("	txn.txn_type as cash_txn_type, ");
            sqlBuilder.append("	txn.txn_amount as txn_amount, txn.txn_date as txn_date, txn.txn_note as txn_note, ");
            sqlBuilder.append("	txn.entity_type as entity_type, txn.entity_id as entity_id, txn.created_date as created_date, ");
            sqlBuilder
                    .append("	o.id as office_id, o.name as office_name, t.id as teller_id, t.name as teller_name, s.display_name as cashier_name ");
            sqlBuilder.append("	from m_cashier_transactions txn ");
            sqlBuilder.append("	left join m_cashiers c on c.id = txn.cashier_id ");
            sqlBuilder.append("	left join m_tellers t on t.id = c.teller_id ");
            sqlBuilder.append("	left join m_office o on o.id = t.office_id ");
            sqlBuilder.append("	left join m_staff s on s.id = c.staff_id ");
            sqlBuilder.append("	where txn.cashier_id = ? ");
            sqlBuilder.append("	and o.hierarchy like ?  ) cashier_txns ");
            sqlBuilder.append("	UNION ");
            sqlBuilder.append("	(select sav_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append("	case ");
            sqlBuilder.append("		when renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge') ");
            sqlBuilder.append("			then 103 ");
            sqlBuilder.append("		when renum.enum_value in ('withdrawal') ");
            sqlBuilder.append("			then 104 ");
            sqlBuilder.append("		else ");
            sqlBuilder.append("			105 ");
            sqlBuilder.append("	end as cash_txn_type, ");
            sqlBuilder.append("	sav_txn.amount as txn_amount, sav_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append("	concat (renum.enum_value, ', Sav:', sav.id, '-', sav.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append("	'savings' as entity_type, sav.id as entity_id, sav_txn.created_date as created_date, ");
            sqlBuilder
                    .append("	o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append("	from m_savings_account_transaction sav_txn ");
            sqlBuilder
                    .append("	left join r_enum_value renum on sav_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'savings_transaction_type_enum' ");
            sqlBuilder.append("	left join m_savings_account sav on sav_txn.savings_account_id = sav.id ");
            sqlBuilder.append("	left join m_client cl on sav.client_id = cl.id ");
            sqlBuilder.append("	left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append("	left join m_appuser user on sav_txn.appuser_id = user.id ");
            sqlBuilder.append("	left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append("	left join m_cashiers c on c.staff_id = staff.id ");
            sqlBuilder.append("	where sav_txn.is_reversed = 0 and c.id = ? ");
            sqlBuilder.append("	and o.hierarchy like ? and ");
            sqlBuilder.append("	created_date between c.start_date and date_add(c.end_date, interval 1 day) ");
            sqlBuilder.append("	) ");
            sqlBuilder.append("	UNION ");
            sqlBuilder.append("	( ");
            sqlBuilder.append("	select loan_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append("	case ");
            sqlBuilder.append("		when renum.enum_value in ('Repayment At Disbursement','Repayment', 'Recovery Payment') ");
            sqlBuilder.append("			then 103 ");
            sqlBuilder.append("		when renum.enum_value in ('Disbursement') ");
            sqlBuilder.append("			then 104 ");
            sqlBuilder.append("		else ");
            sqlBuilder.append("			105 ");
            sqlBuilder.append("	end as cash_txn_type, ");
            sqlBuilder.append("	loan_txn.amount as txn_amount, loan_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append("	concat (renum.enum_value, ', Loan:', loan.id, '-', loan.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append("	'loans' as entity_type, loan.id as entity_id, loan_txn.created_date as created_date, ");
            sqlBuilder
                    .append("	o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append("	from m_loan_transaction loan_txn ");
            sqlBuilder
                    .append("	left join r_enum_value renum on loan_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'transaction_type_enum' ");
            sqlBuilder.append("	left join m_loan loan on loan_txn.loan_id = loan.id ");
            sqlBuilder.append("	left join m_client cl on loan.client_id = cl.id ");
            sqlBuilder.append("	left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append("	left join m_appuser user on loan_txn.appuser_id = user.id ");
            sqlBuilder.append("	left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append("	left join m_cashiers c on c.staff_id = staff.id ");
            sqlBuilder.append("	where loan_txn.is_reversed = 0 and c.id = ? ");
            sqlBuilder.append("	and o.hierarchy like ? ");
            sqlBuilder.append("	and created_date between c.start_date and date_add(c.end_date, interval 1 day) ");
            sqlBuilder.append("	) ");
            sqlBuilder.append("	) txns ");
            sqlBuilder.append("	group by cash_txn_type ");

            return sqlBuilder.toString();
        }

        @Override
        public CashierTransactionTypeTotalsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final Integer cashierTxnType = rs.getInt("cash_txn_type");
            final BigDecimal txnTotal = rs.getBigDecimal("txn_total");

            return CashierTransactionTypeTotalsData.instance(cashierTxnType, txnTotal);
        }
    }

}