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

/* used to show list of parameters used by a report and also for getting a list of parameters available (the reportParameterName is left null */
final public class ReportParameterData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long parameterId;
    @SuppressWarnings("unused")
    private final String parameterName;
    @SuppressWarnings("unused")
    private final String reportParameterName;

    public ReportParameterData(final Long id, final Long parameterId, final String reportParameterName, final String parameterName) {
        this.id = id;
        this.parameterId = parameterId;
        this.parameterName = parameterName;
        this.reportParameterName = reportParameterName;
    }
}