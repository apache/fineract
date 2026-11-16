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

package org.apache.fineract.portfolio.interestratechart.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exceptionmapper.PlatformApiDataValidationExceptionMapper;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InterestRateChartsApiResourceTest {

    @Mock
    private InterestRateChartReadService chartReadPlatformService;
    @Mock
    private CommandDispatcher dispatcher;
    @Mock
    private InterestRateChartData chart;

    private InterestRateChartsApiResource resource;

    @BeforeEach
    void setUp() {
        resource = new InterestRateChartsApiResource(chartReadPlatformService, dispatcher);
    }

    @Test
    void retrieveAllRejectsMissingProductIdBeforeCallingService() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> resource.retrieveAll(null));

        assertEquals(1, exception.getErrors().size());
        assertEquals("productId", exception.getErrors().getFirst().getParameterName());
        assertEquals("validation.msg.interestchart.productId.cannot.be.blank",
                exception.getErrors().getFirst().getUserMessageGlobalisationCode());
        try (Response response = new PlatformApiDataValidationExceptionMapper().toResponse(exception)) {
            assertEquals(400, response.getStatus());
        }
        verifyNoInteractions(chartReadPlatformService, dispatcher);
    }

    @Test
    void retrieveAllReturnsChartsForSpecifiedProduct() {
        Collection<InterestRateChartData> charts = List.of(chart);
        when(chartReadPlatformService.retrieveAllWithSlabs(1L)).thenReturn(charts);

        assertSame(charts, resource.retrieveAll(1L));

        verify(chartReadPlatformService).retrieveAllWithSlabs(1L);
        verifyNoInteractions(dispatcher);
    }

    @Test
    void retrieveAllPreservesEmptyResultForUnknownProduct() {
        Collection<InterestRateChartData> charts = List.of();
        when(chartReadPlatformService.retrieveAllWithSlabs(999999L)).thenReturn(charts);

        assertSame(charts, resource.retrieveAll(999999L));

        verify(chartReadPlatformService).retrieveAllWithSlabs(999999L);
        verifyNoInteractions(dispatcher);
    }
}
