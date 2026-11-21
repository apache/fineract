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
package org.apache.fineract.infrastructure.security.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class TenantOidcConfigRepositoryJdbc implements TenantOidcConfigRepository {

    private static final TenantOidcConfigMapper ROW_MAPPER = new TenantOidcConfigMapper();

    private static final String SELECT_BASE = """
            SELECT id, tenant_id, provider_type, issuer_uri, client_id, client_secret,
                   jwks_uri, username_claim, scopes, post_logout_redirect_uri, enabled
            FROM m_tenant_oidc_config
            """;

    private final JdbcTemplate jdbcTemplate;

    public TenantOidcConfigRepositoryJdbc(@Qualifier("hikariTenantDataSource") DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<TenantOidcConfig> findByIssuerUri(String issuerUri) {
        try {
            TenantOidcConfig result = jdbcTemplate.queryForObject(SELECT_BASE + "WHERE issuer_uri = ? AND enabled = 1", ROW_MAPPER,
                    issuerUri);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<TenantOidcConfig> findByTenantId(String tenantId) {
        try {
            TenantOidcConfig result = jdbcTemplate.queryForObject(SELECT_BASE + "WHERE tenant_id = ? AND enabled = 1", ROW_MAPPER,
                    tenantId);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public TenantOidcConfig save(TenantOidcConfig config) {
        if (config.getId() == null) {
            return insert(config);
        }
        return update(config);
    }

    private TenantOidcConfig insert(TenantOidcConfig config) {
        String sql = """
                INSERT INTO m_tenant_oidc_config
                    (tenant_id, provider_type, issuer_uri, client_id, client_secret,
                     jwks_uri, username_claim, scopes, post_logout_redirect_uri, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            // Named: PostgreSQL returns every column of the row as a generated key otherwise.
            PreparedStatement ps = conn.prepareStatement(sql, new String[] { "id" });
            ps.setString(1, config.getTenantId());
            ps.setString(2, config.getProviderType().name());
            ps.setString(3, config.getIssuerUri());
            ps.setString(4, config.getClientId());
            ps.setString(5, config.getClientSecret());
            ps.setString(6, config.getJwksUri());
            ps.setString(7, config.getUsernameClaim());
            ps.setString(8, config.getScopes());
            ps.setString(9, config.getPostLogoutRedirectUri());
            ps.setInt(10, enabled(config));
            return ps;
        }, keyHolder);

        config.setId(keyHolder.getKey().longValue());
        return config;
    }

    private TenantOidcConfig update(TenantOidcConfig config) {
        String sql = """
                UPDATE m_tenant_oidc_config
                SET provider_type = ?, issuer_uri = ?, client_id = ?, client_secret = ?,
                    jwks_uri = ?, username_claim = ?, scopes = ?, post_logout_redirect_uri = ?,
                    enabled = ?, updated_at = CURRENT_TIMESTAMP(6)
                WHERE tenant_id = ?
                """;

        jdbcTemplate.update(sql, config.getProviderType().name(), config.getIssuerUri(), config.getClientId(), config.getClientSecret(),
                config.getJwksUri(), config.getUsernameClaim(), config.getScopes(), config.getPostLogoutRedirectUri(), enabled(config),
                config.getTenantId());

        return config;
    }

    // A number, as the reads compare it with 1: PostgreSQL makes the TINYINT(1) column a smallint and takes no boolean
    // for it.
    private static int enabled(TenantOidcConfig config) {
        return config.isEnabled() ? 1 : 0;
    }

    @Override
    public void deleteByTenantId(String tenantId) {
        jdbcTemplate.update("DELETE FROM m_tenant_oidc_config WHERE tenant_id = ?", tenantId);
    }

    private static final class TenantOidcConfigMapper implements RowMapper<TenantOidcConfig> {

        @Override
        public TenantOidcConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TenantOidcConfig.builder().id(rs.getLong("id")).tenantId(rs.getString("tenant_id"))
                    .providerType(OidcFederationType.valueOf(rs.getString("provider_type"))).issuerUri(rs.getString("issuer_uri"))
                    .clientId(rs.getString("client_id")).clientSecret(rs.getString("client_secret")).jwksUri(rs.getString("jwks_uri"))
                    .usernameClaim(rs.getString("username_claim")).scopes(rs.getString("scopes"))
                    .postLogoutRedirectUri(rs.getString("post_logout_redirect_uri")).enabled(rs.getBoolean("enabled")).build();
        }
    }
}
