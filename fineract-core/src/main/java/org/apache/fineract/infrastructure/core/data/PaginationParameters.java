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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Builder
@Getter
public class PaginationParameters {

    // TODO: why do we really need this class? SearchParameters seems to provide similar functionality

    public static final int DEFAULT_MAX_LIMIT = 200;

    private boolean paged;
    private Integer offset;
    @Getter(AccessLevel.NONE)
    private Integer limit;
    private String orderBy;
    private String sortOrder;

    public Integer getLimit() {
        if (limit == null) {
            return DEFAULT_MAX_LIMIT;
        }

        if (limit > 0) {
            return limit;
        }

        return null; // unlimited (0 or less)
    }

    public boolean hasOrderBy() {
        return StringUtils.isNotBlank(this.orderBy);
    }

    public boolean hasSortOrder() {
        return StringUtils.isNotBlank(this.sortOrder);
    }

    public boolean hasLimit() {
        return this.limit != null && this.limit > 0;
    }

    public boolean hasOffset() {
        return this.offset != null;
    }

    // TODO: following functions are just doing too much in one place; will disappear with type safe queries

    public String orderBySql() {
        final StringBuilder sql = new StringBuilder();

        if (this.hasOrderBy()) {
            sql.append(" order by ").append(this.getOrderBy());
            if (this.hasSortOrder()) {
                sql.append(' ').append(this.getSortOrder());
            }
        }
        return sql.toString();
    }

    public String limitSql() {
        final StringBuilder sql = new StringBuilder();
        if (this.hasLimit()) {
            sql.append(" limit ").append(this.getLimit());
            if (this.hasOffset()) {
                sql.append(" offset ").append(this.getOffset());
            }
        }
        return sql.toString();
    }

    public String paginationSql() {
        final StringBuilder sqlBuilder = new StringBuilder(50);
        if (this.hasOrderBy()) {
            sqlBuilder.append(' ').append(this.orderBySql());
        }
        if (this.hasLimit()) {
            sqlBuilder.append(' ').append(this.limitSql());
        }

        return sqlBuilder.toString();
    }
}
