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
package org.apache.fineract.portfolio.workingcapitalloan.service;

import static org.mockito.Mockito.verify;

import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachScheduleRepository;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanBreachScheduleDeleteTest {

    private static final Long LOAN_ID = 7L;

    @Mock
    private WorkingCapitalLoanBreachScheduleRepository breachScheduleRepository;
    @Mock
    private WorkingCapitalLoanBreachActionRepository breachActionRepository;
    @Mock
    private WorkingCapitalLoanNearBreachActionRepository nearBreachActionRepository;

    @InjectMocks
    private WorkingCapitalLoanBreachScheduleServiceImpl breachScheduleService;

    @Test
    void deleteScheduleAndActionsRemovesThePeriodsAndTheBreachAndNearBreachActions() {
        breachScheduleService.deleteScheduleAndActions(LOAN_ID);

        verify(breachScheduleRepository).deleteByLoanId(LOAN_ID);
        verify(breachActionRepository).deleteByWorkingCapitalLoanId(LOAN_ID);
        verify(nearBreachActionRepository).deleteByWorkingCapitalLoanId(LOAN_ID);
    }
}
