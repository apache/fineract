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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.fineract.infrastructure.report.annotation.ReportService;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class ReportingProcessServiceProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportingProcessServiceProvider.class);

    private ApplicationContext applicationContext;

    Map<String, String> reportingProcessServices = null;

    ReportingProcessServiceProvider() {
        super();
    }

    public ReportingProcessService findReportingProcessService(final String reportType) {
        if (this.reportingProcessServices.containsKey(reportType)) { return (ReportingProcessService) this.applicationContext
                .getBean(this.reportingProcessServices.get(reportType)); }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.initializeRegistry();
    }

    public Collection<String> findAllReportingTypes() {
        return this.reportingProcessServices.keySet();

    }

    private void initializeRegistry() {
        if (this.reportingProcessServices == null) {
            this.reportingProcessServices = new HashMap<>();

            final String[] reportServiceBeans = this.applicationContext.getBeanNamesForAnnotation(ReportService.class);
            if (ArrayUtils.isNotEmpty(reportServiceBeans)) {
                for (final String reportName : reportServiceBeans) {
                    LOGGER.info("Register report service '" + reportName + "' ...");
                    final ReportService service = this.applicationContext.findAnnotationOnBean(reportName, ReportService.class);
                    try {
                        this.reportingProcessServices.put(service.type(), reportName);
                    } catch (final Throwable th) {
                        LOGGER.error("Unable to register reporting service '" + reportName + "'!", th);
                    }
                }
            }
        }
    }
}
