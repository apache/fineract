package org.mifosplatform.portfolio.collectionsheet.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.collectionsheet.data.ClientLoansData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGClientsData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.mifosplatform.portfolio.collectionsheet.data.JLGCollectionSheetFlatData;
import org.mifosplatform.portfolio.collectionsheet.data.LoanDueData;
import org.mifosplatform.portfolio.group.data.GroupData;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformServiceImpl;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

@Service
public class CollectionSheetReadPlatformServiceImpl implements CollectionSheetReadPlatformService {

    private final PlatformSecurityContext context;
    private final NamedParameterJdbcTemplate namedParameterjdbcTemplate;
    private final GroupReadPlatformServiceImpl groupReadPlatformServiceImpl;

    @Autowired
    public CollectionSheetReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final GroupReadPlatformServiceImpl groupReadPlatformServiceImpl) {
        this.context = context;
        this.namedParameterjdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.groupReadPlatformServiceImpl = groupReadPlatformServiceImpl;
    }

    @Override
    public JLGCollectionSheetData retriveCollectionSheet(final LocalDate dueDate, final Long groupId) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String officeHierarchy = hierarchy + "%";

        final GroupData groupData = this.groupReadPlatformServiceImpl.retrieveGroup(groupId);
        if (groupData == null) { throw new GroupNotFoundException(groupId); }

        final String groupHierarchy = groupData.getHierarchy() + "%";

        final JLGCollectionSheetData jlgCollectionSheetData = buildJLGCollectionSheet(groupHierarchy, officeHierarchy, dueDate);

        return jlgCollectionSheetData;
    }

    /**
     * @param groupHierarchy
     * @param officeHierarchy
     * @param dueDate
     * @return
     * 
     *         Reads all the loans which are due for disbursement or collection
     *         and builds hierarchical data structure for collections sheet with
     *         hierarchy Groups >> Clients >> Loans.
     *         
     */
    @SuppressWarnings("null")
    private JLGCollectionSheetData buildJLGCollectionSheet(final String groupHierarchy, final String officeHierarchy,
            final LocalDate dueDate) {

        final Collection<JLGCollectionSheetFlatData> jlgCollectionSheetFlatData = retriveJLGCollectionSheet(groupHierarchy,
                officeHierarchy, dueDate);

        boolean firstTime = true;
        Long prevGroupId = null;
        Long prevClientId = null;

        final List<JLGClientsData> jlgClientsDatas = new ArrayList<JLGClientsData>();
        List<ClientLoansData> clientLoansDatas = new ArrayList<ClientLoansData>();
        List<LoanDueData> loanDueDatas = new ArrayList<LoanDueData>();

        JLGCollectionSheetData jlgCollectionSheetData = null;
        JLGCollectionSheetFlatData prevCollectioSheetFlatData = null;
        JLGCollectionSheetFlatData corrCollectioSheetFlatData = null;

        if (jlgCollectionSheetFlatData != null) {

            for (final JLGCollectionSheetFlatData collectionSheetFlatData : jlgCollectionSheetFlatData) {

                corrCollectioSheetFlatData = collectionSheetFlatData;

                if (firstTime || collectionSheetFlatData.getGroupId().equals(prevGroupId)) {
                    if (firstTime || collectionSheetFlatData.getClientId().equals(prevClientId)) {
                        if (collectionSheetFlatData.getLoanId() != null) {
                            loanDueDatas.add(collectionSheetFlatData.getLoanDueData());
                        }
                    } else {
                        final ClientLoansData clientLoansData = prevCollectioSheetFlatData.getClientLoansData();
                        clientLoansData.setLoans(loanDueDatas);
                        clientLoansDatas.add(clientLoansData);
                        loanDueDatas = new ArrayList<LoanDueData>();
                        
                        if (collectionSheetFlatData.getLoanId() != null) {
                            loanDueDatas.add(collectionSheetFlatData.getLoanDueData());
                        }

                    }
                } else {

                    final ClientLoansData clientLoansData = prevCollectioSheetFlatData.getClientLoansData();
                    clientLoansData.setLoans(loanDueDatas);
                    clientLoansDatas.add(clientLoansData);

                    final JLGClientsData jlgClientsData = prevCollectioSheetFlatData.getJLGClientsData();
                    jlgClientsData.setClients(clientLoansDatas);

                    jlgClientsDatas.add(jlgClientsData);

                    loanDueDatas = new ArrayList<LoanDueData>();
                    clientLoansDatas = new ArrayList<ClientLoansData>();

                    if (collectionSheetFlatData.getLoanId() != null) {
                        loanDueDatas.add(collectionSheetFlatData.getLoanDueData());
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
            
            final ClientLoansData lastClientLoansData = corrCollectioSheetFlatData.getClientLoansData();
            lastClientLoansData.setLoans(loanDueDatas);
            clientLoansDatas.add(lastClientLoansData);

            final JLGClientsData jlgClientsData = corrCollectioSheetFlatData.getJLGClientsData();
            jlgClientsData.setClients(clientLoansDatas);

            jlgClientsDatas.add(jlgClientsData);

            jlgCollectionSheetData = new JLGCollectionSheetData(dueDate, jlgClientsDatas);
        }

        return jlgCollectionSheetData;
    }

    @Override
    public Collection<JLGCollectionSheetFlatData> retriveJLGCollectionSheet(final String groupHierarchy, final String officeHierarchy,
            final LocalDate dueDate) {

        final JLGCollectionSheetFaltDataMapper mapper = new JLGCollectionSheetFaltDataMapper();
        final String sql = mapper.collectionSheetSchema();

        final SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("hierarchy", groupHierarchy)
                .addValue("dueDate", dueDate).addValue("officeHierarchy", officeHierarchy);
        return this.namedParameterjdbcTemplate.query(sql, namedParameters, mapper);
    }

    private static final class JLGCollectionSheetFaltDataMapper implements RowMapper<JLGCollectionSheetFlatData> {

        public String collectionSheetSchema() {

            return "SELECT  gp.name As groupName, "
                    + "gp.id As groupId, "
                    + "cl.display_name As clientName, "
                    + "sf.id As staffId, "
                    + "sf.display_name As staffName, "
                    + "gl.id As levelId, "
                    + "gl.level_name As levelName, "
                    + "cl.id As clientId, "
                    + "ln.id As loanId, "
                    + "ln.account_no As accountId, "
                    + "ln.loan_status_id As accountStatusId, "
                    + "pl.name As productShortName, "
                    + "ln.product_id As productId, "
                    + "ln.currency_code As currencyCode, "
                    + "ln.currency_digits As currencyDigits, "
                    + "if(ln.loan_status_id = 200 , ln.principal_amount , null) As disbursementAmount, "
                    + "sum(ls.principal_amount - ls.principal_completed_derived) As principalDue, "
                    + "ln.principal_repaid_derived As principalPaid, "
                    + "sum(ls.interest_amount - ls.interest_completed_derived) As interestDue, "
                    + "ln.interest_repaid_derived As interestPaid, "
                    + "sum(lc.amount_outstanding_derived) as chargesDue "
                    + "FROM m_group gp "
                    + "LEFT JOIN m_office of ON of.id = gp.office_id AND of.hierarchy like :officeHierarchy "
                    + "JOIN m_group_level gl ON gl.id = gp.level_Id "
                    + "LEFT JOIN m_staff sf ON sf.id = gp.staff_id "
                    + "JOIN m_group_client gc ON gc.group_id = gp.id "
                    + "JOIN m_client cl ON cl.id = gc.client_id "
                    + "LEFT JOIN m_loan ln ON cl.id = ln.client_id AND ln.group_id is not null AND ( ln.loan_status_id = 300 OR ( ln.loan_status_id =200 AND ln.expected_disbursedon_date <= :dueDate )) "
                    + "LEFT JOIN m_product_loan pl ON pl.id = ln.product_id "
                    + "LEFT JOIN m_loan_repayment_schedule ls ON ls.loan_id = ln.id AND ls.completed_derived = 0 AND ls.duedate <= :dueDate "
                    + "LEFT JOIN m_loan_charge lc ON lc.loan_id = ln.id AND lc.is_paid_derived = 0 AND ( lc.due_for_collection_as_of_date  <= :dueDate OR lc.charge_time_enum = 1) "
                    + "WHERE gp.hierarchy like :hierarchy " + "GROUP BY gp.id ,cl.id , ln.id " + "ORDER BY gp.id , cl.id , ln.id ";
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
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final BigDecimal disbursementAmount = rs.getBigDecimal("disbursementAmount");
            final BigDecimal principalDue = rs.getBigDecimal("principalDue");
            final BigDecimal principalPaid = rs.getBigDecimal("principalPaid");
            final BigDecimal interestDue = rs.getBigDecimal("interestDue");
            final BigDecimal interestPaid = rs.getBigDecimal("interestPaid");
            final BigDecimal chargesDue = rs.getBigDecimal("chargesDue");

            return new JLGCollectionSheetFlatData(groupName, groupId, staffId, staffName, levelId, levelName, clientName, clientId, loanId,
                    accountId, accountStatusId, productShortName, productId, currencyCode, currencyDigits, disbursementAmount,
                    principalDue, principalPaid, interestDue, interestPaid, chargesDue);
        }

    }
}
