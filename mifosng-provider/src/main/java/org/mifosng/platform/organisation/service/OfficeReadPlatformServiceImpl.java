package org.mifosng.platform.organisation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeLookup;
import org.mifosng.data.OfficeTemplateData;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class OfficeReadPlatformServiceImpl implements OfficeReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public OfficeReadPlatformServiceImpl(final PlatformSecurityContext context,
			final DataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	private static final class OfficeMapper implements RowMapper<OfficeData> {

		public String officeSchema() {
			return " o.id as id, o.name as name, o.external_id as externalId, o.opening_date as openingDate, o.hierarchy as hierarchy, parent.id as parentId, parent.name as parentName "
					+ "from org_office o LEFT JOIN org_office AS parent ON parent.id = o.parent_id ";
		}

		@Override
		public OfficeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String externalId = rs.getString("externalId");
			LocalDate openingDate = new LocalDate(rs.getDate("openingDate"));
			String hierarchy = rs.getString("hierarchy");
			Long parentId = rs.getLong("parentId");
			String parentName = rs.getString("parentName");

			return new OfficeData(id, name, externalId, openingDate, hierarchy,
					parentId, parentName);
		}
	}

	private static final class OfficeLookupMapper implements
			RowMapper<OfficeLookup> {

		public String officeLookupSchema() {
			return " o.id as id, o.name as name from org_office o ";
		}

		@Override
		public OfficeLookup mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");

			return new OfficeLookup(id, name);
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
				+ "where o.org_id = ? and o.hierarchy like ? order by o.hierarchy";

		return this.jdbcTemplate.query(sql, rm, new Object[] {
				currentUser.getOrganisation().getId(), hierarchySearchString });
	}

	@Override
	public Collection<OfficeLookup> retrieveAllOfficesForLookup() {
		AppUser currentUser = context.authenticatedUser();

		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";

		OfficeLookupMapper rm = new OfficeLookupMapper();
		String sql = "select "
				+ rm.officeLookupSchema()
				+ "where o.org_id = ? and o.hierarchy like ? order by o.hierarchy";

		return this.jdbcTemplate.query(sql, rm, new Object[] {
				currentUser.getOrganisation().getId(), hierarchySearchString });
	}

	@Override
	public OfficeData retrieveOffice(final Long officeId) {

		try {
			AppUser currentUser = context.authenticatedUser();

			OfficeMapper rm = new OfficeMapper();
			String sql = "select " + rm.officeSchema()
					+ " where o.org_id = ? and o.id = ?";

			OfficeData selectedOffice = this.jdbcTemplate.queryForObject(sql,
					rm, new Object[] { currentUser.getOrganisation().getId(),
							officeId });

			return selectedOffice;
		} catch (EmptyResultDataAccessException e) {
			throw new PlatformResourceNotFoundException(
					"error.msg.office.id.invalid",
					"Office with identifier {0} does not exist.", officeId);
		}
	}

	@Override
	public OfficeTemplateData retrieveNewOfficeTemplate() {

		context.authenticatedUser();

		List<OfficeLookup> parentLookups = new ArrayList<OfficeLookup>(
				retrieveAllOfficesForLookup());

		OfficeTemplateData officeTemplateData = new OfficeTemplateData();
		officeTemplateData.setAllowedParents(parentLookups);
		officeTemplateData.setDefaultOpeningDate(new LocalDate());

		return officeTemplateData;
	}

	@Override
	public OfficeTemplateData retrieveExistingOfficeTemplate(Long officeId) {

		context.authenticatedUser();

		List<OfficeLookup> parentLookups = new ArrayList<OfficeLookup>(
				retrieveAllOfficesForLookup());
		List<OfficeLookup> filterParentLookups = new ArrayList<OfficeLookup>();

		for (OfficeLookup office : parentLookups) {

			if (!office.getId().equals(officeId)) {
				filterParentLookups.add(office);
			}
		}

		OfficeTemplateData officeTemplateData = new OfficeTemplateData();
		officeTemplateData.setAllowedParents(filterParentLookups);
		officeTemplateData.setDefaultOpeningDate(new LocalDate());

		return officeTemplateData;

	}

}