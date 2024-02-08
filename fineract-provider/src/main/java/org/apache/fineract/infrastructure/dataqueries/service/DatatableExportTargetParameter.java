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
package org.apache.fineract.infrastructure.dataqueries.service;

import jakarta.ws.rs.core.MultivaluedMap;

public enum DatatableExportTargetParameter {

    CSV("exportCSV"), PDF("exportPDF"), S3("exportS3"), JSON("exportJSON"), PRETTY_JSON("pretty");

    private final String value;

    DatatableExportTargetParameter(final String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static DatatableExportTargetParameter resolverExportTarget(final MultivaluedMap<String, String> queryParams) {
        for (DatatableExportTargetParameter parameter : DatatableExportTargetParameter.values()) {
            String parameterName = parameter.getValue();
            if (queryParams.getFirst(parameterName) != null) {
                if ("true".equalsIgnoreCase(queryParams.getFirst(parameterName))) {
                    return parameter;
                }
            }
        }
        return JSON;
    }
}
