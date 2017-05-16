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
package org.apache.fineract.organisation.teller.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.data.CashierTransactionTypeTotalsData;
import org.apache.fineract.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.apache.fineract.organisation.teller.data.TellerData;
import org.apache.fineract.organisation.teller.data.TellerJournalData;
import org.apache.fineract.organisation.teller.data.TellerTransactionData;
import org.apache.fineract.organisation.teller.domain.CashierTxnType;
import org.apache.fineract.organisation.teller.domain.TellerStatus;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
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
    private final PaginationHelper<CashierTransactionData> paginationHelper = new PaginationHelper<>();
    private final ColumnValidator columnValidator;

    @Autowired
    public TellerManagementReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, StaffReadPlatformService staffReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.columnValidator = columnValidator;
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
        public TellerData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

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
        public TellerData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

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
        public TellerData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

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
        return retrieveAllTeller(extraCriteria, officeId);
    }

    private Collection<TellerData> retrieveAllTeller(final String extraCriteria, final Long officeId) {

        final TellerMapper tm = new TellerMapper();
        String sql = "select " + tm.schema();
        if (StringUtils.isNotBlank(extraCriteria)) {
            sql += " where " + extraCriteria;
        }
        sql = sql + " order by t.teller_name";
        if(officeId!=null){
        	return this.jdbcTemplate.query(sql, tm, new Object[] {officeId});
        }
        return this.jdbcTemplate.query(sql, tm, new Object[] {});
    }

    private String getTellerCriteria(final String sqlSearch, final Long officeId, final String status) {

        final StringBuffer extraCriteria = new StringBuffer(200);

        if (sqlSearch != null) {
            extraCriteria.append(" and (").append(sqlSearch).append(")");
            final TellerMapper tm = new TellerMapper();
            this.columnValidator.validateSqlInjection(tm.schema(), sqlSearch);
        }
        if (officeId != null) {
            extraCriteria.append(" and office_id = ? ");
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
            final CashierMapper cm = new CashierMapper();
        	this.columnValidator.validateSqlInjection(cm.schema(), sqlSearch);
        	
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

    @Cacheable(value = "tellers", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy()+'of')")
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
            final Date fromDate, final Date toDate, final String currencyCode, final SearchParameters searchParameters) {
        CashierData cashierData = findCashier(cashierId);
        Long staffId = cashierData.getStaffId();
        StaffData staffData = staffReadPlatformService.retrieveStaff(staffId);
        OfficeData officeData = officeReadPlatformService.retrieveOffice(staffData.getOfficeId());
        final String hierarchy = officeData.getHierarchy();
        String hierarchySearchString = null;
        if (includeAllTellers) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy;
        }
        final CashierTransactionSummaryMapper ctsm = new CashierTransactionSummaryMapper();
        final String sql = "select " + ctsm.cashierTxnSummarySchema() + " limit 1000";

        Collection<CashierTransactionTypeTotalsData> cashierTxnTypeTotals = this.jdbcTemplate.query(sql, ctsm, new Object[] { cashierId,
                currencyCode, hierarchySearchString, cashierId, currencyCode, hierarchySearchString, cashierId, currencyCode,
                hierarchySearchString, cashierId, currencyCode, hierarchySearchString });

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

        final Page<CashierTransactionData> cashierTransactions = retrieveCashierTransactions(cashierId, includeAllTellers, fromDate,
                toDate, currencyCode, searchParameters);

        CashierTransactionData cashierTxnTemplate = retrieveCashierTxnTemplate(cashierId);

        CashierTransactionsWithSummaryData txnsWithSummary = CashierTransactionsWithSummaryData.instance(cashierTransactions, allocAmount,
                cashInAmount, cashOutAmount, settleAmount, cashierTxnTemplate.getOfficeName(), cashierTxnTemplate.getTellerId(),
                cashierTxnTemplate.getTellerName(), cashierTxnTemplate.getCashierId(), cashierTxnTemplate.getCashierName());
        return txnsWithSummary;
    }

    @Override
    public Page<CashierTransactionData> retrieveCashierTransactions(final Long cashierId, final boolean includeAllTellers,
            final Date fromDate, final Date toDate, final String currencyCode, final SearchParameters searchParameters) {
        CashierData cashierData = findCashier(cashierId);
        Long staffId = cashierData.getStaffId();
        StaffData staffData = staffReadPlatformService.retrieveStaff(staffId);
        OfficeData officeData = officeReadPlatformService.retrieveOffice(staffData.getOfficeId());
        final String hierarchy = officeData.getHierarchy();
        String hierarchySearchString = null;
        if (includeAllTellers) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy;
        }

        final CashierTransactionMapper ctm = new CashierTransactionMapper();
        
        

        String sql = "select * from (select " + ctm.cashierTxnSchema()
                + " where txn.cashier_id = ? and txn.currency_code = ? and o.hierarchy like ? "
				+ "AND ((case when c.full_day then Date(txn.created_date) between c.start_date AND c.end_date else ( Date(txn.created_date) between c.start_date AND c.end_date"
				+ " ) and ( TIME(txn.created_date) between TIME(c.start_time) AND TIME(c.end_time)) end) or txn.txn_type = 101))  cashier_txns "
				+ " union (select "
                + ctm.savingsTxnSchema()
                + " where sav_txn.is_reversed = 0 and c.id = ? and sav.currency_code = ? and o.hierarchy like ? and "
                + " sav_txn.transaction_date between c.start_date and date_add(c.end_date, interval 1 day) "
                + " and renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge', 'withdrawal', 'Annual Fee', 'Waive Charge', 'Interest Posting', 'Overdraft Interest') "
                + " and (sav_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) "
                + " AND acnttrans.id IS NULL ) "
                + " union (select "
                + ctm.loansTxnSchema()
                + " where loan_txn.is_reversed = 0 and c.id = ? and loan.currency_code = ? and o.hierarchy like ? and "
                + " loan_txn.transaction_date between c.start_date and date_add(c.end_date, interval 1 day) "
                + " and renum.enum_value in ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT','DISBURSEMENT', 'CHARGE_PAYMENT', 'WAIVE_CHARGES', 'WAIVE_INTEREST', 'WRITEOFF') "
                + " and (loan_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) "
                + " AND acnttrans.id IS NULL ) "
                + " union (select "
                + ctm.clientTxnSchema()
                + " where cli_txn.is_reversed = 0 and c.id = ? and cli_txn.currency_code = ? and o.hierarchy like ? and cli_txn.transaction_date "
                + " between c.start_date and date_add(c.end_date, interval 1 day) "
                + " and renum.enum_value in ('PAY_CHARGE', 'WAIVE_CHARGE') "
                + " and (cli_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) ) "
                + " order by created_date ";
        
        if (searchParameters.isLimited()) {
            sql = sql + " limit " + searchParameters.getLimit();
            if (searchParameters.isOffset()) {
                sql = sql + " offset " + searchParameters.getOffset();
            }
        }
        final String sqlCountRows = "SELECT FOUND_ROWS()";
//        return this.jdbcTemplate.query(sql, ctm, new Object[] { cashierId, currencyCode, hierarchySearchString, cashierId, currencyCode,
//                hierarchySearchString, cashierId, currencyCode, hierarchySearchString, cashierId, currencyCode, hierarchySearchString });
        Object[] params = new Object[] {cashierId, currencyCode, hierarchySearchString, cashierId, currencyCode,
                hierarchySearchString, cashierId, currencyCode, hierarchySearchString, cashierId, currencyCode, hierarchySearchString };
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sql, params, ctm);
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
        public CashierData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

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
            sqlBuilder.append(" 	when renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge', 'Annual Fee') ");
            sqlBuilder.append(" 		then 103 ");
            sqlBuilder.append(" 	when renum.enum_value in ('withdrawal', 'Waive Charge', 'Interest Posting', 'Overdraft Interest', '') ");
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
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = sav_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");
            sqlBuilder.append(" left join m_account_transfer_transaction acnttrans ");
            sqlBuilder.append(" on (acnttrans.from_savings_transaction_id = sav_txn.id ");
            sqlBuilder.append(" or acnttrans.to_savings_transaction_id = sav_txn.id) ");

            return sqlBuilder.toString();
        }

        public String loansTxnSchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" loan_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append(" case ");
            sqlBuilder.append(" 	when renum.enum_value in ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT', 'CHARGE_PAYMENT') ");
            sqlBuilder.append(" 		then 103 ");
            sqlBuilder.append(" 	when renum.enum_value in ('DISBURSEMENT', 'WAIVE_INTEREST', 'WRITEOFF', 'WAIVE_CHARGES') ");
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
                    .append(" left join r_enum_value renum on loan_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'loan_transaction_type_enum' ");
            sqlBuilder.append(" left join m_loan loan on loan_txn.loan_id = loan.id ");
            sqlBuilder.append(" left join m_client cl on loan.client_id = cl.id ");
            sqlBuilder.append(" left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append(" left join m_appuser user on loan_txn.appuser_id = user.id ");
            sqlBuilder.append(" left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append(" left join m_cashiers c on c.staff_id = staff.id ");
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = loan_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");
            sqlBuilder.append(" left join m_account_transfer_transaction acnttrans ");
            sqlBuilder.append(" on (acnttrans.from_loan_transaction_id = loan_txn.id ");
            sqlBuilder.append(" or acnttrans.to_loan_transaction_id = loan_txn.id) ");

            return sqlBuilder.toString();
        }

        public String clientTxnSchema() {

            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" cli_txn.id AS txn_id, c.id AS cashier_id, ");
            sqlBuilder.append(" case ");
            sqlBuilder.append(" when renum.enum_value in ('PAY_CHARGE') ");
            sqlBuilder.append(" then 103 ");
            sqlBuilder.append(" when renum.enum_value in ('WAIVE_CHARGE') ");
            sqlBuilder.append(" then 104 ");
            sqlBuilder.append(" else ");
            sqlBuilder.append(" 105 ");
            sqlBuilder.append(" end as cash_txn_type, ");
            sqlBuilder.append(" cli_txn.amount as txn_amount, cli_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append(" concat (renum.enum_value, ', Client:', cl.id, '-', cl.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append(" 'client' as entity_type, cl.id as entity_id, cli_txn.created_date as created_date, ");
            sqlBuilder
                    .append(" o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append(" from m_client_transaction cli_txn ");
            sqlBuilder
                    .append(" left join r_enum_value renum on cli_txn.transaction_type_enum = renum.enum_id AND renum.enum_name = 'client_transaction_type_enum' ");
            sqlBuilder.append(" left join m_client cl on cli_txn.client_id = cl.id ");
            sqlBuilder.append(" left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append(" left join m_appuser user on cli_txn.appuser_id = user.id ");
            sqlBuilder.append(" left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append(" left join m_cashiers c on c.staff_id = staff.id ");
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = cli_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");

            return sqlBuilder.toString();
        }

        @Override
        public CashierTransactionData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

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
			sqlBuilder
					.append(" AND (( case when c.full_day then Date(txn.created_date) between c.start_date AND c.end_date ");
			sqlBuilder
					.append(" else ( Date(txn.created_date) between c.start_date AND c.end_date) and  ( TIME(txn.created_date) between TIME(c.start_time) AND TIME(c.end_time))  end) or txn.txn_type = 101) ");
            sqlBuilder.append(" and   txn.currency_code = ? ");
            sqlBuilder.append("	and o.hierarchy like ?  ) cashier_txns ");
            sqlBuilder.append("	UNION ");
            sqlBuilder.append("	(select sav_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append("	case ");
            sqlBuilder.append("		when renum.enum_value in ('deposit','withdrawal fee', 'Pay Charge', 'Annual Fee') ");
            sqlBuilder.append("			then 103 ");
            sqlBuilder.append("		when renum.enum_value in ('withdrawal', 'Waive Charge', 'Interest Posting', 'Overdraft Interest') ");
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
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = sav_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");
            sqlBuilder.append(" left join m_account_transfer_transaction acnttrans ");
            sqlBuilder.append(" on (acnttrans.from_savings_transaction_id = sav_txn.id ");
            sqlBuilder.append(" or acnttrans.to_savings_transaction_id = sav_txn.id) ");
            sqlBuilder.append("	where sav_txn.is_reversed = 0 and c.id = ? ");
            sqlBuilder.append(" and sav.currency_code = ? ");
            sqlBuilder.append("	and o.hierarchy like ? ");
            sqlBuilder.append("	and sav_txn.transaction_date between c.start_date and date_add(c.end_date, interval 1 day) ");
            sqlBuilder.append("	and (sav_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) ");
            sqlBuilder.append("	AND acnttrans.id IS NULL  ");
            sqlBuilder.append("	) ");
            sqlBuilder.append("	UNION ");
            sqlBuilder.append("	( ");
            sqlBuilder.append("	select loan_txn.id as txn_id, c.id as cashier_id, ");
            sqlBuilder.append("	case ");
            sqlBuilder.append("		when renum.enum_value in ('REPAYMENT_AT_DISBURSEMENT','REPAYMENT', 'RECOVERY_REPAYMENT', 'CHARGE_PAYMENT') ");
            sqlBuilder.append("			then 103 ");
            sqlBuilder.append("		when renum.enum_value in ('DISBURSEMENT', 'WAIVE_INTEREST', 'WRITEOFF', 'WAIVE_CHARGES') ");
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
                    .append("	left join r_enum_value renum on loan_txn.transaction_type_enum = renum.enum_id and renum.enum_name = 'loan_transaction_type_enum' ");
            sqlBuilder.append("	left join m_loan loan on loan_txn.loan_id = loan.id ");
            sqlBuilder.append("	left join m_client cl on loan.client_id = cl.id ");
            sqlBuilder.append("	left join m_office o on cl.office_id = o.id ");
            sqlBuilder.append("	left join m_appuser user on loan_txn.appuser_id = user.id ");
            sqlBuilder.append("	left join m_staff staff on user.staff_id = staff.id ");
            sqlBuilder.append("	left join m_cashiers c on c.staff_id = staff.id ");
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = loan_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");
            sqlBuilder.append(" left join m_account_transfer_transaction acnttrans ");
            sqlBuilder.append(" on (acnttrans.from_loan_transaction_id = loan_txn.id ");
            sqlBuilder.append(" or acnttrans.to_loan_transaction_id = loan_txn.id) ");
            sqlBuilder.append("	where loan_txn.is_reversed = 0 and c.id = ? ");
            sqlBuilder.append(" and loan.currency_code = ? ");
            sqlBuilder.append("	and o.hierarchy like ? ");
            sqlBuilder.append("	and loan_txn.transaction_date between c.start_date and date_add(c.end_date, interval 1 day) ");
            sqlBuilder.append("	and (loan_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1) ");
            sqlBuilder.append("	AND acnttrans.id IS NULL  ");
            sqlBuilder.append("	) ");
            sqlBuilder.append("	UNION ");
            sqlBuilder.append("	( ");
            sqlBuilder.append("	SELECT cli_txn.id AS txn_id, c.id AS cashier_id, ");
            sqlBuilder.append("	case ");
            sqlBuilder.append("		WHEN renum.enum_value IN ('PAY_CHARGE') ");
            sqlBuilder.append("			then 103 ");
            sqlBuilder.append("		WHEN renum.enum_value IN ('WAIVE_CHARGE') ");
            sqlBuilder.append("			then 104 ");
            sqlBuilder.append("		else ");
            sqlBuilder.append("			105 ");
            sqlBuilder.append("	end as cash_txn_type, ");
            sqlBuilder.append("	cli_txn.amount as txn_amount, cli_txn.transaction_date as txn_date, ");
            sqlBuilder
                    .append("	concat (renum.enum_value, ', Client:', cl.id, '-', cl.account_no, ',Client:', cl.id, '-',cl.display_name) as txn_note, ");
            sqlBuilder.append("	'client' as entity_type, cl.id as entity_id, cli_txn.created_date as created_date, ");
            sqlBuilder
                    .append("	o.id as office_id, o.name as office_name, null as teller_id, null as teller_name, staff.display_name as cashier_name ");
            sqlBuilder.append("	from m_client_transaction cli_txn ");
            sqlBuilder
                    .append("	left join r_enum_value renum ON cli_txn.transaction_type_enum = renum.enum_id AND renum.enum_name = 'client_transaction_type_enum' ");
            sqlBuilder.append("	left join m_client cl ON cli_txn.client_id = cl.id ");
            sqlBuilder.append("	left join m_office o ON cl.office_id = o.id ");
            sqlBuilder.append("	left join m_appuser user ON cli_txn.appuser_id = user.id ");
            sqlBuilder.append("	left join m_staff staff ON user.staff_id = staff.id ");
            sqlBuilder.append("	left join m_cashiers c ON c.staff_id = staff.id ");
            sqlBuilder.append(" left join m_payment_detail payDetails on payDetails.id = cli_txn.payment_detail_id ");
            sqlBuilder.append(" left join m_payment_type payType on payType.id = payDetails.payment_type_id ");
            sqlBuilder.append("	where cli_txn.is_reversed = 0 AND c.id = ?    ");
            sqlBuilder.append(" and cli_txn.currency_code = ? ");
            sqlBuilder.append("	and o.hierarchy LIKE ? ");
            sqlBuilder.append("	and cli_txn.transaction_date between c.start_date and date_add(c.end_date, interval 1 day) ");
            sqlBuilder.append(" and (cli_txn.payment_detail_id IS NULL OR payType.is_cash_payment = 1)  ");
            sqlBuilder.append("	) ");
            sqlBuilder.append("	) txns ");
            sqlBuilder.append("	group by cash_txn_type ");

            return sqlBuilder.toString();
        }

        @Override
        public CashierTransactionTypeTotalsData mapRow(final ResultSet rs, final int rowNum)
                throws SQLException {

            final Integer cashierTxnType = rs.getInt("cash_txn_type");
            final BigDecimal txnTotal = rs.getBigDecimal("txn_total");

            return CashierTransactionTypeTotalsData.instance(cashierTxnType, txnTotal);
        }
    }

}