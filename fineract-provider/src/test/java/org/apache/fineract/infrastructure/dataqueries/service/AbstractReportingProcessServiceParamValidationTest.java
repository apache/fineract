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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.dataqueries.data.ReportExportType;
import org.apache.fineract.infrastructure.report.service.AbstractReportingProcessService;
import org.apache.fineract.infrastructure.report.service.ReportParameterTypeResolver;
import org.apache.fineract.infrastructure.security.exception.InputValidationException;
import org.apache.fineract.infrastructure.security.service.InputValidator;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractReportingProcessServiceParamValidationTest {

    private final InputValidator inputValidator = Mockito.mock(InputValidator.class);
    private final ReportParameterTypeResolver resolver = Mockito.mock(ReportParameterTypeResolver.class);

    private final AbstractReportingProcessService service = new AbstractReportingProcessService(inputValidator, resolver) {

        @Override
        public Response processRequest(final String reportName, final MultivaluedMap<String, String> queryParams) {
            return null;
        }

        @Override
        public List<ReportExportType> getAvailableExportTargets() {
            return List.of();
        }
    };

    @Test
    void integerTypedParameterIsValidatedWithTheNumericProfile() {
        Mockito.when(resolver.loadParamFormatTypes("r")).thenReturn(Map.of("officeId", "integer"));
        Mockito.doThrow(new InputValidationException("bad")).when(inputValidator).validate("number", "1' OR '1'='1");

        final MultivaluedMap<String, String> params = new MultivaluedStringMap();
        params.add("R_officeId", "1' OR '1'='1");

        assertThrows(InputValidationException.class, () -> service.getReportParams("r", params));
    }

    @Test
    void validIntegerValueIsAccepted() {
        Mockito.when(resolver.loadParamFormatTypes("r")).thenReturn(Map.of("officeId", "integer"));

        final MultivaluedMap<String, String> params = new MultivaluedStringMap();
        params.add("R_officeId", "12");

        assertEquals(Map.of("${officeId}", "12"), service.getReportParams("r", params));
        Mockito.verify(inputValidator).validate("number", "12");
    }
}
