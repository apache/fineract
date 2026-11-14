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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.apache.fineract.portfolio.loanaccount.repository.ProgressiveLoanModelRepository;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgressiveLoanModelProcessingServiceTest {

    @Mock
    private ProgressiveLoanModelRepository progressiveLoanModelRepository;

    @InjectMocks
    private ProgressiveLoanModelProcessingService service;

    @Test
    void hasValidModelChecksTheCurrentModelVersion() {
        when(progressiveLoanModelRepository.hasValidModel(1L, ProgressiveLoanInterestScheduleModel.getModelVersion())).thenReturn(true);

        assertTrue(service.hasValidModel(1L));
    }
}
