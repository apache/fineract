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

package org.apache.fineract.infrastructure.core.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.apache.fineract.infrastructure.core.service.MdcAdapter;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ContextConfiguration(classes = { Configuration.class })
@WebMvcTest
class FineractCorrelationIdApiFilterTest {

    @SpyBean
    private MdcAdapter mdc;

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = { "/fineract-provider/api/v1/loans", "/fineract-provider/api/v1/loans" })
    void shouldGet200IfXCorrelationIdHeaderIsPresentAndRequestIsForV1Path(String url) throws Exception {
        String correlationId = UUID.randomUUID().toString();
        mockMvc.perform(get(url).header(CorrelationHeaderFilter.correlationIdKey, correlationId)).andExpect(status().isOk())
                .andExpect(header().string(CorrelationHeaderFilter.correlationIdKey, correlationId));

        verify(mdc).remove(CorrelationHeaderFilter.correlationIdKey);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/fineract-provider/api/v1/loans", "/fineract-provider/api/v1/loans" })
    void shouldGet400IfXCorrelationIdHeaderIsNotPresentAndRequestIsForV1Path(String url) throws Exception {
        mockMvc.perform(get(url)).andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist(CorrelationHeaderFilter.correlationIdKey));
    }

    @Test
    void shouldReturnCurrentCorrelationIdFromMDC() {
        MDC.put(CorrelationHeaderFilter.correlationIdKey, "1");
        assertThat(CorrelationHeaderFilter.getCurrentValue()).isEqualTo("1");
    }

}
