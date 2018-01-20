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
package org.apache.fineract.notification.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.notification.cache.CacheNotificationResponseHeader;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.data.NotificationMapperData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class NotificationReadPlatformServiceImpl implements NotificationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ColumnValidator columnValidator;
    private final PaginationHelper<NotificationData> paginationHelper = new PaginationHelper<>();
    private final NotificationDataRow notificationDataRow = new NotificationDataRow();
    private final NotificationMapperRow notificationMapperRow = new NotificationMapperRow();
    private HashMap<Long, HashMap<Long, CacheNotificationResponseHeader>>
            tenantNotificationResponseHeaderCache = new HashMap<>();

    @Autowired
    public NotificationReadPlatformServiceImpl(final RoutingDataSource dataSource,
    		final PlatformSecurityContext context,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.context = context;
        this.columnValidator = columnValidator;
    }

    @Override
    public boolean hasUnreadNotifications(Long appUserId) {
        Long tenantId = ThreadLocalContextUtil.getTenant().getId();
        Long now = System.currentTimeMillis()/1000L;
        if (this.tenantNotificationResponseHeaderCache.containsKey(tenantId)) {
            HashMap<Long, CacheNotificationResponseHeader> notificationResponseHeaderCache =
                    this.tenantNotificationResponseHeaderCache.get(tenantId);
            if (notificationResponseHeaderCache.containsKey(appUserId)) {
                Long lastFetch = notificationResponseHeaderCache.get(appUserId).getLastFetch();
                if ((now - lastFetch) > 1) {
                    return this.createUpdateCacheValue(appUserId, now, notificationResponseHeaderCache);
                } else {
                    return notificationResponseHeaderCache.get(appUserId).hasNotifications();
                }
            } else {
                return this.createUpdateCacheValue(appUserId, now, notificationResponseHeaderCache);
            }
        } else {
            return this.initializeTenantNotificationResponseHeaderCache(tenantId, now, appUserId);

        }
    }

    private boolean initializeTenantNotificationResponseHeaderCache(Long tenantId, Long now, Long appUserId) {
        HashMap<Long, CacheNotificationResponseHeader> notificationResponseHeaderCache = new HashMap<>();
        this.tenantNotificationResponseHeaderCache.put(tenantId, notificationResponseHeaderCache);
        return this.createUpdateCacheValue(appUserId, now, notificationResponseHeaderCache);
    }

    private boolean createUpdateCacheValue(Long appUserId, Long now, HashMap<Long,
            CacheNotificationResponseHeader> notificationResponseHeaderCache) {
        boolean hasNotifications;
        Long tenantId = ThreadLocalContextUtil.getTenant().getId();
        CacheNotificationResponseHeader cacheNotificationResponseHeader;
        hasNotifications = checkForUnreadNotifications(appUserId);
        cacheNotificationResponseHeader = new CacheNotificationResponseHeader(hasNotifications, now);
        notificationResponseHeaderCache.put(appUserId, cacheNotificationResponseHeader);
        this.tenantNotificationResponseHeaderCache.put(tenantId, notificationResponseHeaderCache);
        return hasNotifications;
    }

    private boolean checkForUnreadNotifications(Long appUserId) {
        String sql = "SELECT id, notification_id as notificationId, user_id as userId, is_read as isRead, created_at " +
                "as createdAt FROM notification_mapper WHERE user_id = ? AND is_read = false";
        List<NotificationMapperData > notificationMappers = this.jdbcTemplate.query(
                sql,
                notificationMapperRow,
                appUserId);
        return notificationMappers.size() > 0;
    }

    @Override
    public void updateNotificationReadStatus() {
        final Long appUserId = context.authenticatedUser().getId();
        String sql = "UPDATE notification_mapper SET is_read = true WHERE is_read = false and user_id = ?";
        this.jdbcTemplate.update(sql, appUserId);
    }

    @Override
    public Page<NotificationData> getAllUnreadNotifications(final SearchParameters searchParameters) {
        final Long appUserId = context.authenticatedUser().getId();
        String sql = "SELECT SQL_CALC_FOUND_ROWS ng.id as id, nm.user_id as userId, ng.object_type as objectType, " +
                "ng.object_identifier as objectId, ng.actor as actor, ng.action action, ng.notification_content " +
                "as content, ng.is_system_generated as isSystemGenerated, nm.created_at as createdAt " +
                "FROM notification_mapper nm INNER JOIN notification_generator ng ON nm.notification_id = ng.id " +
                "WHERE nm.user_id = ? AND nm.is_read = false order by nm.created_at desc";

        return getNotificationDataPage(searchParameters, appUserId, sql);
    }


    @Override
    public Page<NotificationData> getAllNotifications(SearchParameters searchParameters) {
        final Long appUserId = context.authenticatedUser().getId();
        String sql = "SELECT SQL_CALC_FOUND_ROWS ng.id as id, nm.user_id as userId, ng.object_type as objectType, " +
                "ng.object_identifier as objectId, ng.actor as actor, ng.action action, ng.notification_content " +
                "as content, ng.is_system_generated as isSystemGenerated, nm.created_at as createdAt " +
                "FROM notification_mapper nm INNER JOIN notification_generator ng ON nm.notification_id = ng.id " +
                "WHERE nm.user_id = ? order by nm.created_at desc";

        return getNotificationDataPage(searchParameters, appUserId, sql);
    }

    private Page<NotificationData> getNotificationDataPage(SearchParameters searchParameters, Long appUserId, String sql) {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append(sql);

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        Object[] params = new Object[]{appUserId};
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                params, this.notificationDataRow);
    }

    private static final class NotificationMapperRow implements RowMapper<NotificationMapperData> {

        @Override
        public NotificationMapperData mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationMapperData notificationMapperData = new NotificationMapperData();

            final Long id = rs.getLong("id");
            notificationMapperData.setId(id);

            final Long notificationId = rs.getLong("notificationId");
            notificationMapperData.setNotificationId(notificationId);

            final Long userId = rs.getLong("userId");
            notificationMapperData.setUserId(userId);

            final boolean isRead = rs.getBoolean("isRead");
            notificationMapperData.setRead(isRead);

            final String createdAt = rs.getString("createdAt");
            notificationMapperData.setCreatedAt(createdAt);

            return notificationMapperData;
        }
    }

    private static final class NotificationDataRow implements RowMapper<NotificationData> {

        @Override
        public NotificationData mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationData notificationData = new NotificationData();

            final Long id = rs.getLong("id");
            notificationData.setId(id);

            final String objectType = rs.getString("objectType");
            notificationData.setObjectType(objectType);

            final Long objectId = rs.getLong("objectId");
            notificationData.entifier(objectId);

            final Long actorId = rs.getLong("actor");
            notificationData.setActor(actorId);

            final String action = rs.getString("action");
            notificationData.setAction(action);

            final String content = rs.getString("content");
            notificationData.setContent(content);

            final boolean isSystemGenerated = rs.getBoolean("isSystemGenerated");
            notificationData.setSystemGenerated(isSystemGenerated);

            final String createdAt = rs.getString("createdAt");
            notificationData.setCreatedAt(createdAt);

            return notificationData;
        }
    }
}