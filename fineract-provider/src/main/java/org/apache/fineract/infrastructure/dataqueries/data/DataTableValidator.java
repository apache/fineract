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
package org.apache.fineract.infrastructure.dataqueries.data;

import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_COLUMN;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_FILTERS;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_OPERATOR;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_QUERY;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_RESULTCOLUMNS;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_TABLE;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.portfolio.search.data.AdvancedQueryData;
import org.apache.fineract.portfolio.search.data.AdvancedQueryRequest;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.data.FilterData;
import org.apache.fineract.portfolio.search.data.TableQueryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataTableValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(
            Arrays.asList(DataTableApiConstant.categoryParamName, DataTableApiConstant.localParamName));

    @Autowired
    public DataTableValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateDataTableRegistration(final String json) {

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DataTableApiConstant.DATATABLE_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(DataTableApiConstant.categoryParamName, element)) {

            final Integer category = this.fromApiJsonHelper.extractIntegerWithLocaleNamed(DataTableApiConstant.categoryParamName, element);
            Object[] objectArray = new Integer[] { DataTableApiConstant.CATEGORY_PPI, DataTableApiConstant.CATEGORY_DEFAULT };
            baseDataValidator.reset().parameter(DataTableApiConstant.categoryParamName).value(category).isOneOfTheseValues(objectArray);
        }

        baseDataValidator.throwValidationErrors();
    }

    public void validateTableSearch(@NotNull AdvancedQueryRequest queryRequest) {
        final List<ApiParameterError> errors = new ArrayList<>();
        final DataValidatorBuilder validator = new DataValidatorBuilder(errors).resource(DataTableApiConstant.DATATABLE_RESOURCE_NAME);
        AdvancedQueryData baseQuery = queryRequest.getBaseQuery();
        if (baseQuery != null) {
            validateQueryData(baseQuery, validator);
        }
        List<TableQueryData> datatableQueries = queryRequest.getDatatableQueries();
        if (datatableQueries != null) {
            for (TableQueryData datatableQuery : datatableQueries) {
                validator.reset().parameter(API_PARAM_TABLE).value(datatableQuery.getTable()).notBlank();
                AdvancedQueryData queryData = datatableQuery.getQuery();
                validator.reset().parameter(API_PARAM_QUERY).value(queryData).notBlank();
                if (queryData != null) {
                    validateQueryData(queryData, validator);
                }
            }
        }
        validator.throwValidationErrors();
    }

    public void validateTableSearch(@NotNull AdvancedQueryData queryData) {
        final DataValidatorBuilder validator = new DataValidatorBuilder(new ArrayList<>())
                .resource(DataTableApiConstant.DATATABLE_RESOURCE_NAME);
        validateQueryData(queryData, validator);
        validator.throwValidationErrors();
    }

    private void validateQueryData(@NotNull AdvancedQueryData queryData, @NotNull DataValidatorBuilder validator) {
        List<ColumnFilterData> columnFilters = queryData.getColumnFilters();
        if (columnFilters != null) {
            for (ColumnFilterData columnFilter : columnFilters) {
                validator.reset().parameter(API_PARAM_COLUMN).value(columnFilter.getColumn()).notNull();
                List<FilterData> filters = columnFilter.getFilters();
                validator.reset().parameter(API_PARAM_FILTERS).value(filters == null ? null : filters.toArray()).notNull().arrayNotEmpty();
                if (filters != null) {
                    for (FilterData filter : filters) {
                        validator.reset().parameter(API_PARAM_OPERATOR).value(filter.getOperator()).notNull();
                    }
                }
            }
            List<String> resultColumns = queryData.getResultColumns();
            if (resultColumns != null) {
                for (String resultColumn : resultColumns) {
                    validator.reset().parameter(API_PARAM_RESULTCOLUMNS).value(resultColumn).notBlank();
                }
            }
        }
    }
}
