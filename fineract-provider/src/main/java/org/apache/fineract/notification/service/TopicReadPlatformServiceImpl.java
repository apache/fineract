package org.apache.fineract.notification.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.TopicData;
import org.apache.fineract.notification.exception.TopicNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TopicReadPlatformServiceImpl implements TopicReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    @Autowired
	public TopicReadPlatformServiceImpl(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
		this.jdbcTemplate = jdbcTemplate;
		this.context = context;
	}
    
    private static final class TopicMapper implements RowMapper<TopicData> {

        private final String schema;

        public TopicMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("t.id as id, t.name as name, t.permission_id ass permissionId, ");
            sqlBuilder.append("t.is_active as isActive, t.activation_date as activationDate, ");
            sqlBuilder.append("from topic t ");
            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public TopicData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final Long permissionId = rs.getLong("permissionId");
            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Boolean isActive = rs.getBoolean("isActive");

            return new TopicData(id, name, permissionId, isActive, activationDate);
        }

    }

	@Override
	public Collection<TopicData> getAllTopics() {
		final AppUser currentUser = this.context.authenticatedUser();
		
		final TopicMapper rm = new TopicMapper();
		String sql = "select " + rm.schema();
		
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";
		
		return this.jdbcTemplate.query(sql, rm, new Object[] { hierarchySearchString });
	}

	@Override
	public Collection<TopicData> getAllActiveTopics() {
		final TopicMapper rm = new TopicMapper();

        final String sql = " select " + rm.schema() + " where t.is_active = ?";

        return this.jdbcTemplate.query(sql, rm, new Object[] { true });
	}

	@Override
	public TopicData findById(Long topicId) {
		try {
            final TopicMapper rm = new TopicMapper();

            final String sql = " select " + rm.schema() + " where t.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { topicId });
        } catch (final EmptyResultDataAccessException e) {
            throw new TopicNotFoundException(topicId);
        }
	}

	@Override
	public TopicData findByPermissionId(Long permissionId) {
		try {
            final TopicMapper rm = new TopicMapper();

            final String sql = " select " + rm.schema() + " where t.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { permissionId });
        } catch (final EmptyResultDataAccessException e) {
            throw new TopicNotFoundException(permissionId);
        }
	}

	
}
