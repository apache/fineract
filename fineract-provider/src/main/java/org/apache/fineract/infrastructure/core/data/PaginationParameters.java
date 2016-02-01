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
package org.apache.fineract.infrastructure.core.data;

import org.apache.commons.lang.StringUtils;

/**
 * <p>
 * Immutable data object representing pagination parameter values.
 * </p>
 */
public class PaginationParameters {

    private final boolean paged;
    private final Integer offset;
    private final Integer limit;
    private final String orderBy;
    private final String sortOrder;

    public static PaginationParameters instance(Boolean paged, Integer offset, Integer limit, String orderBy, String sortOrder) {
        if (null == paged) {
            paged = false;
        }

        final Integer maxLimitAllowed = getCheckedLimit(limit);

        return new PaginationParameters(paged, offset, maxLimitAllowed, orderBy, sortOrder);
    }

    private PaginationParameters(boolean paged, Integer offset, Integer limit, String orderBy, String sortOrder) {
        this.paged = paged;
        this.offset = offset;
        this.limit = limit;
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
    }

    public static Integer getCheckedLimit(final Integer limit) {

        final Integer maxLimitAllowed = 200;
        // default to max limit first off
        Integer checkedLimit = maxLimitAllowed;

        if (limit != null && limit > 0) {
            checkedLimit = limit;
        } else if (limit != null) {
            // unlimited case: limit provided and 0 or less
            checkedLimit = null;
        }

        return checkedLimit;
    }

    public boolean isPaged() {
        return this.paged;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public Integer getLimit() {
        return this.limit;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public boolean isOrderByRequested() {
        return StringUtils.isNotBlank(this.orderBy);
    }

    public boolean isSortOrderProvided() {
        return StringUtils.isNotBlank(this.sortOrder);
    }

    public boolean isLimited() {
        return this.limit != null && this.limit.intValue() > 0;
    }

    public boolean isOffset() {
        return this.offset != null;
    }

    public String orderBySql() {
        final StringBuffer sql = new StringBuffer();

        if (this.isOrderByRequested()) {
            sql.append(" order by ").append(this.getOrderBy());
            if (this.isSortOrderProvided()) {
                sql.append(' ').append(this.getSortOrder());
            }
        }
        return sql.toString();
    }

    public String limitSql() {
        final StringBuffer sql = new StringBuffer();
        if (this.isLimited()) {
            sql.append(" limit ").append(this.getLimit());
            if (this.isOffset()) {
                sql.append(" offset ").append(this.getOffset());
            }
        }
        return sql.toString();
    }
    
    public String paginationSql(){
        final StringBuilder sqlBuilder = new StringBuilder(50); 
        if (this.isOrderByRequested()) {
            sqlBuilder.append(' ').append(this.orderBySql());
        }        
        if (this.isLimited()) {
            sqlBuilder.append(' ').append(this.limitSql());
        }
        
        return sqlBuilder.toString();
    }
}