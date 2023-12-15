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
package org.apache.fineract.portfolio.search.data;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.SqlOperator;

/**
 * Immutable data object representing datatable data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ColumnFilterData implements Serializable {

    private String column;

    private List<FilterData> filters;

    public static ColumnFilterData eq(String column, String value) {
        return new ColumnFilterData(column, List.of(FilterData.eq(value)));
    }

    public static ColumnFilterData btw(String column, String value1, String value2) {
        return new ColumnFilterData(column, List.of(FilterData.btw(value1, value2)));
    }

    public static ColumnFilterData create(String column, SqlOperator op, String... values) {
        return new ColumnFilterData(column, List.of(FilterData.create(op, values)));
    }
}
