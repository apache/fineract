package org.mifosplatform.accounting.accountmapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.accountmapping.data.OfficeToGLAccountMappingData;
import org.mifosplatform.accounting.accountmapping.exception.OfficeToGLAccountMappingNotFoundException;
import org.mifosplatform.accounting.common.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OfficeToGLAccountMappingReadPlatformServiceImpl implements OfficeToGLAccountMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final OfficeToGLAccountMappingMapper accountMappingMapper;
    private final PaginationHelper<OfficeToGLAccountMappingData> paginationHelper = new PaginationHelper<OfficeToGLAccountMappingData>();
    private final OfficeReadPlatformService officeReadPlatformService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;

    @Autowired
    public OfficeToGLAccountMappingReadPlatformServiceImpl(final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService) {
        accountMappingMapper = new OfficeToGLAccountMappingMapper();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeReadPlatformService = officeReadPlatformService;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
    }

    @Override
    public Page<OfficeToGLAccountMappingData> retrieveAll(final SearchParameters searchParameters) {
        StringBuilder sqlBuilder = new StringBuilder(300);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.accountMappingMapper.schema());
        Object[] finalObjectArray = {};
        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                this.accountMappingMapper);
    }

    @Override
    public OfficeToGLAccountMappingData retrieve(Long mappingId) {
        try {
            StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.accountMappingMapper.schema());
            sqlBuilder.append(" where om.id=?");
            return this.jdbcTemplate.queryForObject(sqlBuilder.toString(), this.accountMappingMapper, new Object[] { mappingId });
        } catch (final EmptyResultDataAccessException e) {
            throw new OfficeToGLAccountMappingNotFoundException(mappingId);
        }
    }

    @Override
    public OfficeToGLAccountMappingData retrieveTemplate() {
        Collection<OfficeData> officeOptions = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Map<String, List<GLAccountData>> accountOptions = this.accountingDropdownReadPlatformService.retrieveAccountMappingOptions();
        return OfficeToGLAccountMappingData.template(accountOptions, officeOptions);
    }

    private static final class OfficeToGLAccountMappingMapper implements RowMapper<OfficeToGLAccountMappingData> {

        private final String sql;

        public OfficeToGLAccountMappingMapper() {
            StringBuilder sb = new StringBuilder(300);
            sb.append("om.id , office.id as officeId, office.name as officeName, ");
            sb.append(" glaccount.id as glAccountId,glaccount.name as glAccountName,glaccount.gl_code as glCode  ");
            sb.append(" from acc_gl_office_mapping om ");
            sb.append("inner join m_office office on office.id = om.office_id ");
            sb.append("inner join acc_gl_account glaccount on glaccount.id = om.gl_account_id");
            sql = sb.toString();
        }

        public String schema() {
            return sql;
        }

        @Override
        public OfficeToGLAccountMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final String nameDecorated = null;
            final String externalId = null;
            final LocalDate openingDate = null;
            final String hierarchy = null;
            final Long parentId = null;
            final String parentName = null;
            final Collection<OfficeData> allowedParents = null;
            final OfficeData officeData = new OfficeData(officeId, officeName, nameDecorated, externalId, openingDate, hierarchy, parentId,
                    parentName, allowedParents);

            final Long glAccountId = JdbcSupport.getLong(rs, "glAccountId");
            final String glAccountName = rs.getString("glAccountName");
            final String glCode = rs.getString("glCode");

            final GLAccountData gLAccountData = new GLAccountData(glAccountId, glAccountName, glCode);

            final OfficeToGLAccountMappingData accountMappingData = OfficeToGLAccountMappingData.instance(id, officeData, gLAccountData);
            return accountMappingData;
        }

    }

}
