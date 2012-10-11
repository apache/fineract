package org.mifosng.platform.documentmanagement.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.DocumentData;
import org.mifosng.platform.client.service.ClientReadPlatformService;
import org.mifosng.platform.exceptions.DocumentNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.loan.service.LoanReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.staff.service.StaffReadPlatformService;
import org.mifosng.platform.user.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DocumentReadPlatformServiceImpl implements
		DocumentReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	// TODO: use these services to check if passed in entities for document
	// management exists (and ensure appropriate errors are thrown)
	private final PlatformSecurityContext context;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final ClientReadPlatformService clientReadPlatformService;
	private final LoanReadPlatformService loanReadPlatformService;
	private final StaffReadPlatformService staffReadPlatformService;
	private final AppUserReadPlatformService appUserReadPlatformService;

	@Autowired
	public DocumentReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource,
			final OfficeReadPlatformService officeReadPlatformService,
			final AppUserReadPlatformService appUserReadPlatformService,
			final ClientReadPlatformService clientReadPlatformService,
			final LoanReadPlatformService loanReadPlatformService,
			final StaffReadPlatformService staffReadPlatformService) {
		this.context = context;
		this.officeReadPlatformService = officeReadPlatformService;
		this.appUserReadPlatformService = appUserReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.clientReadPlatformService = clientReadPlatformService;
		this.loanReadPlatformService = loanReadPlatformService;
		this.staffReadPlatformService = staffReadPlatformService;
	}

	@Override
	public Collection<DocumentData> retrieveAllDocuments(String entityType,
			Long entityId) {
		// TODO verify if the entities are valid and a user
		// has data
		// scope for the particular entities
		DocumentMapper mapper = new DocumentMapper();
		String sql = "select " + mapper.schema() + " order by d.id";
		return this.jdbcTemplate.query(sql, mapper, new Object[] { entityType,
				entityId });
	}

	@Override
	public DocumentData retrieveDocument(String entityType, Long entityId,
			Long documentId) {
		try {
			// TODO verify if the entities are valid and a
			// user has data
			// scope for the particular entities
			DocumentMapper mapper = new DocumentMapper();
			String sql = "select " + mapper.schema() + " and d.id=? ";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {
					entityType, entityId, documentId });
		} catch (EmptyResultDataAccessException e) {
			throw new DocumentNotFoundException(entityType, entityId,
					documentId);
		}
	}

	private static final class DocumentMapper implements
			RowMapper<DocumentData> {

		public DocumentMapper() {
		}

		public String schema() {
			return "d.id as id, d.parent_entity_type as parentEntityType, d.parent_entity_id as parentEntityId, d.name as name, "
					+ " d.file_name as fileName, d.size as fileSize, d.type as fileType, "
					+ " d.description as description, d.location as location"
					+ " from m_document d where d.parent_entity_type=? and d.parent_entity_id=?";
		}

		@Override
		public DocumentData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = JdbcSupport.getLong(rs, "id");
			Long parentEntityId = JdbcSupport.getLong(rs, "parentEntityId");
			Long fileSize = JdbcSupport.getLong(rs, "fileSize");
			String parentEntityType = rs.getString("parentEntityType");
			String name = rs.getString("name");
			String fileName = rs.getString("fileName");
			String fileType = rs.getString("fileType");
			String description = rs.getString("description");
			String location = rs.getString("location");

			return new DocumentData(id, parentEntityType, parentEntityId, name,
					fileName, fileSize, fileType, description, location);
		}
	}

}