/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.service;

import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.calendarIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.transactionDateParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.officeIdParamName;
import static org.mifosplatform.portfolio.collectionsheet.CollectionSheetConstants.staffIdParamName;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.codes.service.CodeValueReadPlatformService;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.calendar.domain.Calendar;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRepositoryWrapper;
import org.mifosplatform.portfolio.calendar.exception.NotValidRecurringDateException;
import org.mifosplatform.portfolio.collectionsheet.data.IndividualClientData;
import org.mifosplatform.portfolio.collectionsheet.data.IndividualCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.IndividualCollectionSheetLoanFlatData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGClientData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetFlatData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGGroupData;
import org.mifosplatform.portfolio.collectionsheet.data.LoanDueData;
import org.mifosplatform.portfolio.collectionsheet.data.SavingsDueData;
import org.mifosplatform.portfolio.collectionsheet.serialization.CollectionSheetGenerateCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.group.data.CenterData;
import org.mifosplatform.portfolio.group.data.GroupGeneralData;
import org.mifosplatform.portfolio.group.service.CenterReadPlatformService;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.meeting.attendance.service.AttendanceDropdownReadPlatformService;
import org.mifosplatform.portfolio.meeting.attendance.service.AttendanceEnumerations;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

@Service
public class CollectionSheetReadPlatformServiceImpl implements CollectionSheetReadPlatformService {

    private final PlatformSecurityContext context;
    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final CenterReadPlatformService centerReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CollectionSheetGenerateCommandFromApiJsonDeserializer collectionSheetGenerateCommandFromApiJsonDeserializer;
    private final CalendarRepositoryWrapper calendarRepositoryWrapper;
    private final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService;
    private final MandatorySavingsCollectionsheetExtractor mandatorySavingsExtractor = new MandatorySavingsCollectionsheetExtractor();
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public CollectionSheetReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final CenterReadPlatformService centerReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final CollectionSheetGenerateCommandFromApiJsonDeserializer collectionSheetGenerateCommandFromApiJsonDeserializer,
            final CalendarRepositoryWrapper calendarRepositoryWrapper,
            final AttendanceDropdownReadPlatformService attendanceDropdownReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.centerReadPlatformService = centerReadPlatformService;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.collectionSheetGenerateCommandFromApiJsonDeserializer = collectionSheetGenerateCommandFromApiJsonDeserializer;
        this.groupReadPlatformService = groupReadPlatformService;
        this.calendarRepositoryWrapper = calendarRepositoryWrapper;
        this.attendanceDropdownReadPlatformService = attendanceDropdownReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    /*
     * Reads all the loans which are due for disbursement or collection and
     * builds hierarchical data structure for collections sheet with hierarchy
     * Groups >> Clients >> Loans.
     */
    @SuppressWarnings("null")
    private JLGCollectionSheetData buildJLGCollectionSheet(final LocalDate dueDate,
            final Collection<JLGCollectionSheetFlatData> jlgCollectionSheetFlatData) {

        boolean firstTime = true;
        Long prevGroupId = null;
        Long prevClientId = null;
        final Collection<PaymentTypeData> paymentOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
                

        final List<JLGGroupData> jlgGroupsData = new ArrayList<>();
        List<JLGClientData> clientsData = new ArrayList<>();
        List<LoanDueData> loansDueData = new ArrayList<>();

        JLGCollectionSheetData jlgCollectionSheetData = null;
        JLGCollectionSheetFlatData prevCollectioSheetFlatData = null;
        JLGCollectionSheetFlatData corrCollectioSheetFlatData = null;
        final Set<LoanProductData> loanProducts = new HashSet<>();
        if (jlgCollectionSheetFlatData != null) {

            for (final JLGCollectionSheetFlatData collectionSheetFlatData : jlgCollectionSheetFlatData) {

                if (collectionSheetFlatData.getProductId() != null) {
                    loanProducts.add(LoanProductData.lookupWithCurrency(collectionSheetFlatData.getProductId(),
                            collectionSheetFlatData.getProductShortName(), collectionSheetFlatData.getCurrency()));
                }
                corrCollectioSheetFlatData = collectionSheetFlatData;

                if (firstTime || collectionSheetFlatData.getGroupId().equals(prevGroupId)) {
                    if (firstTime || collectionSheetFlatData.getClientId().equals(prevClientId)) {
                        if (collectionSheetFlatData.getLoanId() != null) {
                            loansDueData.add(collectionSheetFlatData.getLoanDueData());
                        }
                    } else {
                        final JLGClientData clientData = prevCollectioSheetFlatData.getClientData();
                        clientData.setLoans(loansDueData);
                        clientsData.add(clientData);
                        loansDueData = new ArrayList<>();

                        if (collectionSheetFlatData.getLoanId() != null) {
                            loansDueData.add(collectionSheetFlatData.getLoanDueData());
                        }

                    }
                } else {

                    final JLGClientData clientData = prevCollectioSheetFlatData.getClientData();
                    clientData.setLoans(loansDueData);
                    clientsData.add(clientData);

                    final JLGGroupData jlgGroupData = prevCollectioSheetFlatData.getJLGGroupData();
                    jlgGroupData.setClients(clientsData);

                    jlgGroupsData.add(jlgGroupData);

                    loansDueData = new ArrayList<>();
                    clientsData = new ArrayList<>();

                    if (collectionSheetFlatData.getLoanId() != null) {
                        loansDueData.add(collectionSheetFlatData.getLoanDueData());
                    }
                }

                prevClientId = collectionSheetFlatData.getClientId();
                prevGroupId = collectionSheetFlatData.getGroupId();
                prevCollectioSheetFlatData = collectionSheetFlatData;
                firstTime = false;
            }

            // FIXME Need to check last loan is added under previous
            // client/group or new client / previous group or new client / new
            // group
            if (corrCollectioSheetFlatData != null) {
                final JLGClientData lastClientData = corrCollectioSheetFlatData.getClientData();
                lastClientData.setLoans(loansDueData);
                clientsData.add(lastClientData);

                final JLGGroupData jlgGroupData = corrCollectioSheetFlatData.getJLGGroupData();
                jlgGroupData.setClients(clientsData);
                jlgGroupsData.add(jlgGroupData);
            }

            jlgCollectionSheetData = JLGCollectionSheetData.instance(dueDate, loanProducts, jlgGroupsData,
                    this.attendanceDropdownReadPlatformService.retrieveAttendanceTypeOptions(), paymentOptions);
        }

        return jlgCollectionSheetData;
    }

    private static final class JLGCollectionSheetFaltDataMapper implements RowMapper<JLGCollectionSheetFlatData> {

        public String collectionSheetSchema(final boolean isCenterCollection) {
            StringBuffer sql = new StringBuffer(400);
            sql.append("SELECT loandata.*, sum(lc.amount_outstanding_derived) as chargesDue from ")
                    .append("(SELECT gp.display_name As groupName, ")
                    .append("gp.id As groupId, ")
                    .append("cl.display_name As clientName, ")
                    .append("sf.id As staffId, ")
                    .append("sf.display_name As staffName, ")
                    .append("gl.id As levelId, ")
                    .append("gl.level_name As levelName, ")
                    .append("cl.id As clientId, ")
                    .append("ln.id As loanId, ")
                    .append("ln.account_no As accountId, ")
                    .append("ln.loan_status_id As accountStatusId, ")
                    .append("pl.short_name As productShortName, ")
                    .append("ln.product_id As productId, ")
                    .append("ln.currency_code as currencyCode, ln.currency_digits as currencyDigits, ln.currency_multiplesof as inMultiplesOf, rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ")
                    .append("if(ln.loan_status_id = 200 , ln.principal_amount , null) As disbursementAmount, ")
                    .append("sum(ifnull(if(ln.loan_status_id = 300, ls.principal_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.principal_completed_derived, 0.0), 0.0)) As principalDue, ")
                    .append("ln.principal_repaid_derived As principalPaid, ")
                    .append("sum(ifnull(if(ln.loan_status_id = 300, ls.interest_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.interest_completed_derived, 0.0), 0.0)) As interestDue, ")
                    .append("ln.interest_repaid_derived As interestPaid, ")
                    .append("ca.attendance_type_enum as attendanceTypeId ")
                    .append("FROM m_group gp ")
                    .append("LEFT JOIN m_office of ON of.id = gp.office_id AND of.hierarchy like :officeHierarchy ")
                    .append("JOIN m_group_level gl ON gl.id = gp.level_Id ")
                    .append("LEFT JOIN m_staff sf ON sf.id = gp.staff_id ")
                    .append("JOIN m_group_client gc ON gc.group_id = gp.id ")
                    .append("JOIN m_client cl ON cl.id = gc.client_id ")
                    .append("LEFT JOIN m_loan ln ON cl.id = ln.client_id  and ln.group_id=gp.id AND ln.group_id is not null AND ( ln.loan_status_id = 300 OR ( ln.loan_status_id =200 AND ln.expected_disbursedon_date <= :dueDate )) ")
                    .append("LEFT JOIN m_product_loan pl ON pl.id = ln.product_id ")
                    .append("LEFT JOIN m_currency rc on rc.`code` = ln.currency_code ")
                    .append("LEFT JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate ")
                    .append("left join m_calendar_instance ci on gp.parent_id = ci.entity_id and ci.entity_type_enum =:entityTypeId ")
                    .append("left join m_meeting mt on ci.id = mt.calendar_instance_id and mt.meeting_date =:dueDate ")
                    .append("left join m_client_attendance ca on ca.meeting_id=mt.id and ca.client_id=cl.id ");

            if (isCenterCollection) {
                sql.append("WHERE gp.parent_id = :centerId ");
            } else {
                sql.append("WHERE gp.id = :groupId ");
            }

            sql.append("and (gp.status_enum = 300 or (gp.status_enum = 600 and gp.closedon_date >= :dueDate)) ")
                    .append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ")
                    .append("GROUP BY gp.id ,cl.id , ln.id ORDER BY gp.id , cl.id , ln.id ").append(") loandata ")
                    .append("LEFT JOIN m_loan_charge lc ON lc.loan_id = loandata.loanId AND lc.is_paid_derived = 0 AND lc.is_active = 1 ")
                    .append("AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) ")
                    .append("GROUP BY loandata.groupId, ").append("loandata.clientId, ").append("loandata.loanId ")
                    .append("ORDER BY loandata.groupId, ").append("loandata.clientId, ").append("loandata.loanId ");

            return sql.toString();

        }

        @Override
        public JLGCollectionSheetFlatData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String groupName = rs.getString("groupName");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final Long levelId = JdbcSupport.getLong(rs, "levelId");
            final String levelName = rs.getString("levelName");
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

            final Integer attendanceTypeId = rs.getInt("attendanceTypeId");
            final EnumOptionData attendanceType = AttendanceEnumerations.attendanceType(attendanceTypeId);

            return new JLGCollectionSheetFlatData(groupName, groupId, staffId, staffName, levelId, levelName, clientName, clientId, loanId,
                    accountId, accountStatusId, productShortName, productId, currencyData, disbursementAmount, principalDue, principalPaid,
                    interestDue, interestPaid, chargesDue, attendanceType);
        }

    }

    @Override
    public JLGCollectionSheetData generateGroupCollectionSheet(final Long groupId, final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheet(query.json());

        final Long calendarId = query.longValueOfParameterNamed(calendarIdParamName);
        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String transactionDateStr = df.format(transactionDate.toDate());

        final Calendar calendar = this.calendarRepositoryWrapper.findOneWithNotFoundDetection(calendarId);
        // check if transaction against calendar effective from date

        if (!calendar.isValidRecurringDate(transactionDate)) { throw new NotValidRecurringDateException("collectionsheet", "The date '"
                + transactionDate + "' is not a valid meeting date.", transactionDate); }

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);

        final JLGCollectionSheetFaltDataMapper mapper = new JLGCollectionSheetFaltDataMapper();

        // entityType should be center if it's within a center
        final CalendarEntityType entityType = (group.isChildGroup()) ? CalendarEntityType.CENTERS : CalendarEntityType.GROUPS;

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", transactionDateStr)
                .addValue("groupId", group.getId()).addValue("officeHierarchy", officeHierarchy)
                .addValue("entityTypeId", entityType.getValue());

        final Collection<JLGCollectionSheetFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(
                mapper.collectionSheetSchema(false), namedParameters, mapper);

        // loan data for collection sheet
        JLGCollectionSheetData collectionSheetData = buildJLGCollectionSheet(transactionDate, collectionSheetFlatDatas);

        // mandatory savings data for collection sheet
        Collection<JLGGroupData> groupsWithSavingsData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(false), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeSavingsGroupDataIntoCollectionsheetData(groupsWithSavingsData, collectionSheetData);

        collectionSheetData = JLGCollectionSheetData.withSavingsProducts(collectionSheetData,
                retrieveSavingsProducts(groupsWithSavingsData));

        return collectionSheetData;
    }

    private void mergeSavingsGroupDataIntoCollectionsheetData(final Collection<JLGGroupData> groupsWithSavingsData,
            final JLGCollectionSheetData collectionSheetData) {
        final List<JLGGroupData> groupsWithLoanData = (List<JLGGroupData>) collectionSheetData.getGroups();
        for (JLGGroupData groupSavingsData : groupsWithSavingsData) {
            if (groupsWithLoanData.contains(groupSavingsData)) {
                mergeGroup(groupSavingsData, groupsWithLoanData);
            } else {
                groupsWithLoanData.add(groupSavingsData);
            }
        }

    }

    private void mergeGroup(final JLGGroupData groupSavingsData, final List<JLGGroupData> groupsWithLoanData) {
        final int index = groupsWithLoanData.indexOf(groupSavingsData);

        if (index < 0) return;

        JLGGroupData groupLoanData = groupsWithLoanData.get(index);
        List<JLGClientData> clientsLoanData = (List<JLGClientData>) groupLoanData.getClients();
        List<JLGClientData> clientsSavingsData = (List<JLGClientData>) groupSavingsData.getClients();

        for (JLGClientData clientSavingsData : clientsSavingsData) {
            if (clientsLoanData.contains(clientSavingsData)) {
                mergeClient(clientSavingsData, clientsLoanData);
            } else {
                clientsLoanData.add(clientSavingsData);
            }
        }
    }

    private void mergeClient(final JLGClientData clientSavingsData, List<JLGClientData> clientsLoanData) {
        final int index = clientsLoanData.indexOf(clientSavingsData);

        if (index < 0) return;

        JLGClientData clientLoanData = clientsLoanData.get(index);
        clientLoanData.setSavings(clientSavingsData.getSavings());
    }

    private Collection<SavingsProductData> retrieveSavingsProducts(Collection<JLGGroupData> groupsWithSavingsData) {
        List<SavingsProductData> savingsProducts = new ArrayList<>();
        for (JLGGroupData groupSavingsData : groupsWithSavingsData) {
            Collection<JLGClientData> clientsSavingsData = groupSavingsData.getClients();
            for (JLGClientData clientSavingsData : clientsSavingsData) {
                Collection<SavingsDueData> savingsDatas = clientSavingsData.getSavings();
                for (SavingsDueData savingsDueData : savingsDatas) {
                    final SavingsProductData savingsProduct = SavingsProductData.lookup(savingsDueData.productId(),
                            savingsDueData.productName());
                    if (!savingsProducts.contains(savingsProduct)) {
                        savingsProducts.add(savingsProduct);
                    }
                }
            }
        }
        return savingsProducts;
    }

    @Override
    public JLGCollectionSheetData generateCenterCollectionSheet(final Long centerId, final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheet(query.json());

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final CenterData center = this.centerReadPlatformService.retrieveOne(centerId);

        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String dueDateStr = df.format(transactionDate.toDate());

        final JLGCollectionSheetFaltDataMapper mapper = new JLGCollectionSheetFaltDataMapper();

        StringBuilder sql = new StringBuilder(mapper.collectionSheetSchema(true));

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", dueDateStr)
                .addValue("centerId", center.getId()).addValue("officeHierarchy", officeHierarchy)
                .addValue("entityTypeId", CalendarEntityType.CENTERS.getValue());

        final Collection<JLGCollectionSheetFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(sql.toString(),
                namedParameters, mapper);

        // loan data for collection sheet
        JLGCollectionSheetData collectionSheetData = buildJLGCollectionSheet(transactionDate, collectionSheetFlatDatas);

        // mandatory savings data for collection sheet
        Collection<JLGGroupData> groupsWithSavingsData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(true), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeSavingsGroupDataIntoCollectionsheetData(groupsWithSavingsData, collectionSheetData);

        collectionSheetData = JLGCollectionSheetData.withSavingsProducts(collectionSheetData,
                retrieveSavingsProducts(groupsWithSavingsData));

        return collectionSheetData;
    }

    private static final class MandatorySavingsCollectionsheetExtractor implements ResultSetExtractor<Collection<JLGGroupData>> {

        private final GroupSavingsDataMapper groupSavingsDataMapper = new GroupSavingsDataMapper();

        public String collectionSheetSchema(final boolean isCenterCollection) {

            final StringBuffer sql = new StringBuffer(400);
            sql.append("SELECT gp.display_name As groupName, ")
                    .append("gp.id As groupId, ")
                    .append("cl.display_name As clientName, ")
                    .append("cl.id As clientId, ")
                    .append("sf.id As staffId, ")
                    .append("sf.display_name As staffName, ")
                    .append("gl.id As levelId, ")
                    .append("gl.level_name As levelName, ")
                    .append("sa.id As savingsId, ")
                    .append("sa.account_no As accountId, ")
                    .append("sa.status_enum As accountStatusId, ")
                    .append("sp.short_name As productShortName, ")
                    .append("sp.id As productId, ")
                    .append("sa.currency_code as currencyCode, ")
                    .append("sa.currency_digits as currencyDigits, ")
                    .append("sa.currency_multiplesof as inMultiplesOf, ")
                    .append("rc.`name` as currencyName, ")
                    .append("rc.display_symbol as currencyDisplaySymbol, ")
                    .append("rc.internationalized_name_code as currencyNameCode, ")
                    .append("sum(ifnull(mss.deposit_amount,0) - ifnull(mss.deposit_amount_completed_derived,0)) as dueAmount ")

                    .append("FROM m_group gp ")
                    .append("LEFT JOIN m_office of ON of.id = gp.office_id AND of.hierarchy like :officeHierarchy ")
                    .append("JOIN m_group_level gl ON gl.id = gp.level_Id ")
                    .append("LEFT JOIN m_staff sf ON sf.id = gp.staff_id ")
                    .append("JOIN m_group_client gc ON gc.group_id = gp.id ")
                    .append("JOIN m_client cl ON cl.id = gc.client_id ")
                    .append("JOIN m_savings_account sa ON sa.client_id=cl.id and sa.status_enum=300 ")
                    .append("JOIN m_savings_product sp ON sa.product_id=sp.id ")
                    .append("JOIN m_deposit_account_recurring_detail dard ON sa.id = dard.savings_account_id AND dard.is_mandatory = true AND dard.is_calendar_inherited = true ")
                    .append("JOIN m_mandatory_savings_schedule mss ON mss.savings_account_id=sa.id AND mss.duedate <= :dueDate ")
                    .append("LEFT JOIN m_currency rc on rc.`code` = sa.currency_code ");

            if (isCenterCollection) {
                sql.append("WHERE gp.parent_id = :centerId ");
            } else {
                sql.append("WHERE gp.id = :groupId ");
            }

            sql.append("and (gp.status_enum = 300 or (gp.status_enum = 600 and gp.closedon_date >= :dueDate)) ")
                    .append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ")
                    .append("GROUP BY gp.id ,cl.id , sa.id ORDER BY gp.id , cl.id , sa.id ");

            return sql.toString();
        }

        @Override
        public Collection<JLGGroupData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<JLGGroupData> groups = new ArrayList<>();

            JLGGroupData group = null;
            int groupIndex = 0;
            boolean isEndOfRecords = false;
            // move cursor to first row.
            final boolean isNotEmtyResultSet = rs.next();

            if (isNotEmtyResultSet) {
                while (!isEndOfRecords) {
                    group = groupSavingsDataMapper.mapRowData(rs, groupIndex++);
                    groups.add(group);
                    isEndOfRecords = rs.isAfterLast();
                }
            }

            return groups;
        }
    }

    private static final class GroupSavingsDataMapper implements RowMapper<JLGGroupData> {

        private final ClientSavingsDataMapper clientSavingsDataMapper = new ClientSavingsDataMapper();

        private GroupSavingsDataMapper() {}

        public JLGGroupData mapRowData(ResultSet rs, int rowNum) throws SQLException {
            final List<JLGClientData> clients = new ArrayList<>();
            final JLGGroupData group = this.mapRow(rs, rowNum);
            final Long previousGroupId = group.getGroupId();

            // first client row of new group
            JLGClientData client = clientSavingsDataMapper.mapRowData(rs, rowNum);
            clients.add(client);

            // if its not after last row loop
            while (!rs.isAfterLast()) {
                final Long groupId = JdbcSupport.getLong(rs, "groupId");
                if (previousGroupId != null && groupId.compareTo(previousGroupId) != 0) {
                    // return for next group details
                    return JLGGroupData.withClients(group, clients);
                }
                client = clientSavingsDataMapper.mapRowData(rs, rowNum);
                clients.add(client);
            }

            return JLGGroupData.withClients(group, clients);
        }

        @Override
        public JLGGroupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final String groupName = rs.getString("groupName");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");
            final Long levelId = JdbcSupport.getLong(rs, "levelId");
            final String levelName = rs.getString("levelName");
            return JLGGroupData.instance(groupId, groupName, staffId, staffName, levelId, levelName);
        }
    }

    private static final class ClientSavingsDataMapper implements RowMapper<JLGClientData> {

        private final SavingsDueDataMapper savingsDueDataMapper = new SavingsDueDataMapper();

        private ClientSavingsDataMapper() {}

        public JLGClientData mapRowData(ResultSet rs, int rowNum) throws SQLException {

            List<SavingsDueData> savings = new ArrayList<>();

            JLGClientData client = this.mapRow(rs, rowNum);
            final Long previousClientId = client.getClientId();

            // first savings row of new client record
            SavingsDueData saving = savingsDueDataMapper.mapRow(rs, rowNum);
            savings.add(saving);

            while (rs.next()) {
                final Long clientId = JdbcSupport.getLong(rs, "clientId");
                if (previousClientId != null && clientId.compareTo(previousClientId) != 0) {
                    // client id changes then return for next client data
                    return JLGClientData.withSavings(client, savings);
                }
                saving = savingsDueDataMapper.mapRow(rs, rowNum);
                savings.add(saving);
            }
            return JLGClientData.withSavings(client, savings);
        }

        @Override
        public JLGClientData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {

            final String clientName = rs.getString("clientName");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            // final Integer attendanceTypeId = rs.getInt("attendanceTypeId");
            // final EnumOptionData attendanceType =
            // AttendanceEnumerations.attendanceType(attendanceTypeId);
            final EnumOptionData attendanceType = null;

            return JLGClientData.instance(clientId, clientName, attendanceType);
        }
    }

    private static final class SavingsDueDataMapper implements RowMapper<SavingsDueData> {

        private SavingsDueDataMapper() {}

        @Override
        public SavingsDueData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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
            // currency
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf,
                    currencyDisplaySymbol, currencyNameCode);

            return SavingsDueData.instance(savingsId, accountId, accountStatusId, productName, productId, currency, dueAmount);
        }
    }

    @Override
    public IndividualCollectionSheetData generateIndividualCollectionSheet(final JsonQuery query) {

        this.collectionSheetGenerateCommandFromApiJsonDeserializer.validateForGenerateCollectionSheetOfIndividuals(query.json());

        final LocalDate transactionDate = query.localDateValueOfParameterNamed(transactionDateParamName);
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String transactionDateStr = df.format(transactionDate.toDate());

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final Long officeId = query.longValueOfParameterNamed(officeIdParamName);
        final Long staffId = query.longValueOfParameterNamed(staffIdParamName);
        final boolean checkForOfficeId = officeId != null;
        final boolean checkForStaffId = staffId != null;

        final IndividualCollectionSheetFaltDataMapper mapper = new IndividualCollectionSheetFaltDataMapper(checkForOfficeId,
                checkForStaffId);

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("dueDate", transactionDateStr).addValue(
                "officeHierarchy", officeHierarchy);

        if (checkForOfficeId) {
            ((MapSqlParameterSource) namedParameters).addValue("officeId", officeId);
        }
        if (checkForStaffId) {
            ((MapSqlParameterSource) namedParameters).addValue("staffId", staffId);
        }

        final Collection<IndividualCollectionSheetLoanFlatData> collectionSheetFlatDatas = this.namedParameterjdbcTemplate.query(
                mapper.sqlSchema(), namedParameters, mapper);

        IndividualMandatorySavingsCollectionsheetExtractor mandatorySavingsExtractor = new IndividualMandatorySavingsCollectionsheetExtractor(
                checkForOfficeId, checkForStaffId);
        // mandatory savings data for collection sheet
        Collection<IndividualClientData> clientData = this.namedParameterjdbcTemplate.query(
                mandatorySavingsExtractor.collectionSheetSchema(), namedParameters, mandatorySavingsExtractor);

        // merge savings data into loan data
        mergeLoanData(collectionSheetFlatDatas, (List<IndividualClientData>) clientData);

        Collection<CodeValueData> paymentOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(PaymentDetailConstants.paymentTypeCodeName);

        return IndividualCollectionSheetData.instance(transactionDate, clientData, paymentOptions);

    }

    private static final class IndividualCollectionSheetFaltDataMapper implements RowMapper<IndividualCollectionSheetLoanFlatData> {

        private final String sql;

        public IndividualCollectionSheetFaltDataMapper(final boolean checkForOfficeId, final boolean checkforStaffId) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT loandata.*, sum(lc.amount_outstanding_derived) as chargesDue ");
            sb.append("from (SELECT cl.display_name As clientName, ");
            sb.append("cl.id As clientId, ln.id As loanId, ln.account_no As accountId, ln.loan_status_id As accountStatusId,");
            sb.append(" pl.short_name As productShortName, ln.product_id As productId, ");
            sb.append("ln.currency_code as currencyCode, ln.currency_digits as currencyDigits, ln.currency_multiplesof as inMultiplesOf, ");
            sb.append("rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ");
            sb.append("if(ln.loan_status_id = 200 , ln.principal_amount , null) As disbursementAmount, ");
            sb.append("sum(ifnull(if(ln.loan_status_id = 300, ls.principal_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.principal_completed_derived, 0.0), 0.0)) As principalDue, ");
            sb.append("ln.principal_repaid_derived As principalPaid, ");
            sb.append("sum(ifnull(if(ln.loan_status_id = 300, ls.interest_amount, 0.0), 0.0) - ifnull(if(ln.loan_status_id = 300, ls.interest_completed_derived, 0.0), 0.0)) As interestDue, ");
            sb.append("ln.interest_repaid_derived As interestPaid ");
            sb.append("FROM m_loan ln ");
            sb.append("JOIN m_client cl ON cl.id = ln.client_id  ");
            sb.append("LEFT JOIN m_office of ON of.id = cl.office_id  AND of.hierarchy like :officeHierarchy ");
            sb.append("LEFT JOIN m_product_loan pl ON pl.id = ln.product_id ");
            sb.append("LEFT JOIN m_currency rc on rc.`code` = ln.currency_code ");
            sb.append("JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate ");
            sb.append("where ");
            if (checkForOfficeId) {
                sb.append("of.id = :officeId and ");
            }
            if (checkforStaffId) {
                sb.append("ln.loan_officer_id = :staffId and ");
            }
            sb.append("(ln.loan_status_id = 300) ");
            sb.append("and ln.group_id is null GROUP BY cl.id , ln.id ORDER BY cl.id , ln.id ) loandata ");
            sb.append("LEFT JOIN m_loan_charge lc ON lc.loan_id = loandata.loanId AND lc.is_paid_derived = 0 AND lc.is_active = 1 AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) ");
            sb.append("GROUP BY loandata.clientId, loandata.loanId ORDER BY loandata.clientId, loandata.loanId ");

            sql = sb.toString();
        }

        public String sqlSchema() {
            return sql;
        }

        @Override
        public IndividualCollectionSheetLoanFlatData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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

            return new IndividualCollectionSheetLoanFlatData(clientName, clientId, loanId, accountId, accountStatusId, productShortName,
                    productId, currencyData, disbursementAmount, principalDue, principalPaid, interestDue, interestPaid, chargesDue);
        }

    }

    private static final class IndividualMandatorySavingsCollectionsheetExtractor implements
            ResultSetExtractor<Collection<IndividualClientData>> {

        private final SavingsDueDataMapper savingsDueDataMapper = new SavingsDueDataMapper();

        private final String sql;

        public IndividualMandatorySavingsCollectionsheetExtractor(final boolean checkForOfficeId, final boolean checkforStaffId) {

            final StringBuffer sb = new StringBuffer(400);
            sb.append("SELECT cl.display_name As clientName, cl.id As clientId, ");
            sb.append("sa.id As savingsId, sa.account_no As accountId, sa.status_enum As accountStatusId, ");
            sb.append("sp.short_name As productShortName, sp.id As productId, ");
            sb.append("sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");
            sb.append("rc.`name` as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, ");
            sb.append("sum(ifnull(mss.deposit_amount,0) - ifnull(mss.deposit_amount_completed_derived,0)) as dueAmount ");
            sb.append("FROM m_savings_account sa ");
            sb.append("JOIN m_client cl ON cl.id = sa.client_id ");
            sb.append("JOIN m_savings_product sp ON sa.product_id=sp.id ");
            sb.append("JOIN m_deposit_account_recurring_detail dard ON sa.id = dard.savings_account_id AND dard.is_mandatory = true AND dard.is_calendar_inherited = false ");
            sb.append("JOIN m_mandatory_savings_schedule mss ON mss.savings_account_id=sa.id AND mss.completed_derived = 0 AND mss.duedate <= :dueDate ");
            sb.append("LEFT JOIN m_office of ON of.id = cl.office_id AND of.hierarchy like :officeHierarchy ");
            sb.append("LEFT JOIN m_currency rc on rc.`code` = sa.currency_code ");
            sb.append("WHERE sa.status_enum=300 and sa.group_id is null ");
            sb.append("and (cl.status_enum = 300 or (cl.status_enum = 600 and cl.closedon_date >= :dueDate)) ");
            if (checkForOfficeId) {
                sb.append("and of.id = :officeId ");
            }
            if (checkforStaffId) {
                sb.append("and sa.field_officer_id = :staffId ");
            }
            sb.append("GROUP BY cl.id , sa.id ORDER BY cl.id , sa.id ");

            this.sql = sb.toString();
        }

        public String collectionSheetSchema() {
            return this.sql;
        }

        @SuppressWarnings("null")
        @Override
        public Collection<IndividualClientData> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<IndividualClientData> clientData = new ArrayList<>();
            int rowNum = 0;

            IndividualClientData client = null;
            Long previousClientId = null;

            while (rs.next()) {
                final Long clientId = JdbcSupport.getLong(rs, "clientId");
                if (previousClientId == null || clientId.compareTo(previousClientId) != 0) {
                    final String clientName = rs.getString("clientName");
                    client = IndividualClientData.instance(clientId, clientName);
                    client = IndividualClientData.withSavings(client, new ArrayList<SavingsDueData>());
                    clientData.add(client);
                    previousClientId = clientId;
                }
                SavingsDueData saving = savingsDueDataMapper.mapRow(rs, rowNum);
                client.addSavings(saving);
                rowNum++;
            }

            return clientData;
        }
    }

    private void mergeLoanData(final Collection<IndividualCollectionSheetLoanFlatData> loanFlatDatas, List<IndividualClientData> clientDatas) {

        IndividualClientData clientSavingsData = null;
        for (IndividualCollectionSheetLoanFlatData loanFlatData : loanFlatDatas) {
            IndividualClientData clientData = loanFlatData.getClientData();
            if (clientSavingsData == null || !clientSavingsData.equals(clientData)) {
                if (clientDatas.contains(clientData)) {
                    final int index = clientDatas.indexOf(clientData);
                    if (index < 0) return;
                    clientSavingsData = clientDatas.get(index);
                    clientSavingsData.setLoans(new ArrayList<LoanDueData>());
                } else {
                    clientSavingsData = clientData;
                    clientSavingsData.setLoans(new ArrayList<LoanDueData>());
                    clientDatas.add(clientSavingsData);
                }
            }
            clientSavingsData.addLoans(loanFlatData.getLoanDueData());
        }
    }
}
