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
package org.apache.fineract.infrastructure.report.service;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.security.service.SqlValidator;

public abstract class AbstractReportingProcessService implements ReportingProcessService {

    private final SqlValidator sqlValidator;

    protected AbstractReportingProcessService(SqlValidator sqlValidator) {
        this.sqlValidator = sqlValidator;
    }

    @Override
    public Map<String, String> getReportParams(final MultivaluedMap<String, String> queryParams) {
        final Map<String, String> reportParams = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            if (entry.getKey().startsWith("R_")) {
                String pKey = "${" + entry.getKey().substring(2) + "}";
                String pValue = entry.getValue().get(0);
                sqlValidator.validate(pValue);
                reportParams.put(pKey, pValue);
            }
        }
        return reportParams;
    }
}
