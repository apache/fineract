package org.mifosng.platform.organisation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OfficeReadPlatformServiceImpl implements OfficeReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final static String nameDecoratedBaseOnHierarchy = "concat(substring('........................................', 1, ((LENGTH(o.hierarchy) - LENGTH(REPLACE(o.hierarchy, '.', '')) - 1) * 4)), o.name)";

	@Autowired
	public OfficeReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class OfficeMapper implements RowMapper<OfficeData> {

		public String officeSchema() {
			return " o.id as id, o.name as name, "
					+ nameDecoratedBaseOnHierarchy
					+ " as nameDecorated, o.external_id as externalId, o.opening_date as openingDate, o.hierarchy as hierarchy, parent.id as parentId, parent.name as parentName "
					+ "from org_office o LEFT JOIN org_office AS parent ON parent.id = o.parent_id ";
		}

		@Override
		public OfficeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String nameDecorated = rs.getString("nameDecorated");
			String externalId = rs.getString("externalId");
			LocalDate openingDate = JdbcSupport.getLocalDate(rs, "openingDate");
			String hierarchy = rs.getString("hierarchy");
			Long parentId = JdbcSupport.getLong(rs, "parentId");
			String parentName = rs.getString("parentName");

			return new OfficeData(id, name, nameDecorated, externalId,
					openingDate, hierarchy, parentId, parentName);
		}
	}

	private static final class OfficeLookupMapper implements
			RowMapper<OfficeLookup> {

		public String officeLookupSchema() {
			return " o.id as id, " + nameDecoratedBaseOnHierarchy
					+ " as nameDecorated, o.name as name from org_office o ";
		}

		@Override
		public OfficeLookup mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String nameDecorated = rs.getString("nameDecorated");

			return new OfficeLookup(id, name, nameDecorated);
		}
	}

	@Override
	public Collection<OfficeData> retrieveAllOffices() {

		AppUser currentUser = context.authenticatedUser();

		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";

		OfficeMapper rm = new OfficeMapper();
		String sql = "select "
				+ rm.officeSchema()
				+ "where o.hierarchy like ? order by o.hierarchy";

		return this.jdbcTemplate.query(sql, rm, new Object[] {hierarchySearchString});
	}

	@Override
	public Collection<OfficeLookup> retrieveAllOfficesForLookup() {
		AppUser currentUser = context.authenticatedUser();

		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";

		OfficeLookupMapper rm = new OfficeLookupMapper();
		String sql = "select "
				+ rm.officeLookupSchema()
				+ "where o.hierarchy like ? order by o.hierarchy";

		return this.jdbcTemplate.query(sql, rm, new Object[] {hierarchySearchString});
	}

	@Override
	public OfficeData retrieveOffice(final Long officeId) {

		try {
			context.authenticatedUser();

			OfficeMapper rm = new OfficeMapper();
			String sql = "select " + rm.officeSchema() + " where o.id = ?";

			OfficeData selectedOffice = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {officeId});

			return selectedOffice;
		} catch (EmptyResultDataAccessException e) {
			throw new OfficeNotFoundException(officeId);
		}
	}

	@Override
	public OfficeData retrieveNewOfficeTemplate() {

		context.authenticatedUser();

		List<OfficeLookup> parentLookups = new ArrayList<OfficeLookup>(
				retrieveAllOfficesForLookup());

		OfficeData officeData = new OfficeData();
		officeData.setAllowedParents(parentLookups);
		officeData.setOpeningDate(new LocalDate());

		return officeData;
	}

	@Override
	public List<OfficeLookup> retrieveAllowedParents(Long officeId) {

		context.authenticatedUser();

		List<OfficeLookup> parentLookups = new ArrayList<OfficeLookup>(
				retrieveAllOfficesForLookup());
		List<OfficeLookup> filterParentLookups = new ArrayList<OfficeLookup>();

		for (OfficeLookup office : parentLookups) {

			if (!office.getId().equals(officeId)) {
				filterParentLookups.add(office);
			}
		}

		return filterParentLookups;

	}
}