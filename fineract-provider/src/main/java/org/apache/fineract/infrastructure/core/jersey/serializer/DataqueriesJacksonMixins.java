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
package org.apache.fineract.infrastructure.core.jersey.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import java.util.Map;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.ReportData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnValueData;

/**
 * Jackson mixins that make the datatable and report data classes serialize their fields, as Gson did. They are
 * registered on the application mapper only. Annotating the classes themselves would change the generated OpenAPI spec
 * and client models.
 */
public final class DataqueriesJacksonMixins {

    public static final Map<Class<?>, Class<?>> MIXINS = Map.of(//
            DatatableData.class, DatatableDataMixin.class, //
            ReportData.class, ReportDataMixin.class, //
            ResultsetColumnHeaderData.class, ResultsetColumnHeaderDataMixin.class, //
            ResultsetColumnValueData.class, ResultsetColumnValueDataMixin.class);

    private DataqueriesJacksonMixins() {}

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
    abstract static class DatatableDataMixin {}

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
    abstract static class ReportDataMixin {}

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
    abstract static class ResultsetColumnHeaderDataMixin {}

    @JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
    abstract static class ResultsetColumnValueDataMixin {}
}
