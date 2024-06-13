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
package org.apache.fineract.infrastructure.report.provider;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.report.annotation.ReportService;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class ReportingProcessServiceProvider {

    public static final String SERVICE_MISSING = "There is no ReportingProcessService registered in the ReportingProcessServiceProvider for this report type: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingProcessServiceProvider.class);

    private final Map<String, ReportingProcessService> reportingProcessServices;

    @Autowired
    public ReportingProcessServiceProvider(List<ReportingProcessService> reportingProcessServices) {
        var mapBuilder = ImmutableMap.<String, ReportingProcessService>builder();
        for (ReportingProcessService s : reportingProcessServices) {
            String[] reportTypes = s.getClass().getAnnotation(ReportService.class).type();
            for (String type : reportTypes) {
                mapBuilder.put(type, s);
            }
            LOGGER.info("Registered report service '{}' for type/s '{}'", s, reportTypes);
        }
        this.reportingProcessServices = mapBuilder.build();
    }

    public ReportingProcessService findReportingProcessService(final String reportType) {
        return reportingProcessServices.getOrDefault(reportType, null);
    }

    public Collection<String> findAllReportingTypes() {
        return this.reportingProcessServices.keySet();
    }
}
