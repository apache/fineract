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
package org.apache.fineract.api;

import java.io.OutputStream;
import java.util.Map;

public interface ReportingProcessService {

    enum ReportType {

        HTML("text/html"), PDF("application/pdf"), XLS("application/vnd.ms-excel"), XLSX(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), CSV("text/csv");

        ReportType(String contentType) {
            this.contentType = contentType;
        }

        private final String contentType;

        public String getContentType() {
            return contentType;
        }
    }

    String PARAM_OUTPUT_TYPE = "output-type";
    String PARAM_LOCALE = "locale";
    String PARAM_TENANT_URL = "tenantUrl";
    String PARAM_USER_HIERARCHIE = "userhierarchy";
    String PARAM_USERNAME = "username";
    String PARAM_PASSWORD = "password";
    String PARAM_USER_ID = "userid";
    String PARAM_DATABASE_ULR = "databaseUrl";
    String PARAMETER_PREFIX = "R_";

    void process(String name, Map<String, String> parameter, OutputStream os);

    String getType();
}
