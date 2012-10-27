package org.mifosng.platform.documentmanagement.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.DocumentData;
import org.mifosng.platform.exceptions.DocumentNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DocumentReadPlatformServiceImpl implements DocumentReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	// TODO: use these services to check if passed in entities for document
	// management exists (and ensure appropriate errors are thrown)
	private final PlatformSecurityContext context;
//	private final OfficeReadPlatformService officeReadPlatformService;
//	private final ClientReadPlatformService clientReadPlatformService;
//	private final LoanReadPlatformService loanReadPlatformService;
//	private final StaffReadPlatformService staffReadPlatformService;
//	private final AppUserReadPlatformService appUserReadPlatformService;

	@Autowired
	public DocumentReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource
//			final OfficeReadPlatformService officeReadPlatformService,
//			final AppUserReadPlatformService appUserReadPlatformService,
//			final ClientReadPlatformService clientReadPlatformService,
//			final LoanReadPlatformService loanReadPlatformService,
//			final StaffReadPlatformService staffReadPlatformService
			) {
		this.context = context;
//		this.officeReadPlatformService = officeReadPlatformService;
//		this.appUserReadPlatformService = appUserReadPlatformService;
//		this.clientReadPlatformService = clientReadPlatformService;
//		this.loanReadPlatformService = loanReadPlatformService;
//		this.staffReadPlatformService = staffReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<DocumentData> retrieveAllDocuments(final String entityType, final Long entityId) {
		
		context.authenticatedUser();
		
		// TODO verify if the entities are valid and a user
		// has data
		// scope for the particular entities
		DocumentMapper mapper = new DocumentMapper();
		String sql = "select " + mapper.schema() + " order by d.id";
		return this.jdbcTemplate.query(sql, mapper, new Object[] {entityType, entityId});
	}

	@Override
	public DocumentData retrieveDocument(final String entityType, final Long entityId, final Long documentId) {
		
		try {
			context.authenticatedUser();
			
			// TODO verify if the entities are valid and a
			// user has data
			// scope for the particular entities
			DocumentMapper mapper = new DocumentMapper();
			String sql = "select " + mapper.schema() + " and d.id=? ";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {entityType, entityId, documentId });
		} catch (EmptyResultDataAccessException e) {
			throw new DocumentNotFoundException(entityType, entityId, documentId);
		}
	}

	private static final class DocumentMapper implements RowMapper<DocumentData> {

		public String schema() {
			return "d.id as id, d.parent_entity_type as parentEntityType, d.parent_entity_id as parentEntityId, d.name as name, "
					+ " d.file_name as fileName, d.size as fileSize, d.type as fileType, "
					+ " d.description as description, d.location as location"
					+ " from m_document d where d.parent_entity_type=? and d.parent_entity_id=?";
		}

		@Override
		public DocumentData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

			final Long id = JdbcSupport.getLong(rs, "id");
			final Long parentEntityId = JdbcSupport.getLong(rs, "parentEntityId");
			final Long fileSize = JdbcSupport.getLong(rs, "fileSize");
			final String parentEntityType = rs.getString("parentEntityType");
			final String name = rs.getString("name");
			final String fileName = rs.getString("fileName");
			final String fileType = rs.getString("fileType");
			final String description = rs.getString("description");
			final String location = rs.getString("location");

			return new DocumentData(id, parentEntityType, parentEntityId, name,
					fileName, fileSize, fileType, description, location);
		}
	}
}