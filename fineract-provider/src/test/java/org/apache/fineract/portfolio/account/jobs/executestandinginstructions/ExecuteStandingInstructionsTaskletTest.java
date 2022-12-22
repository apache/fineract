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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.account.data.StandingInstructionDuesData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExecuteStandingInstructionsTaskletTest {
    private static ExecuteStandingInstructionsTasklet executeStandingInstructionsTasklet;
    @Autowired
    private  AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    @Autowired
    private  JdbcTemplate jdbcTemplate;
    @Autowired
    private StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    @Autowired
    private DatabaseSpecificSQLGenerator sqlGenerator;

    @BeforeEach
    void setUp() {
        executeStandingInstructionsTasklet= new ExecuteStandingInstructionsTasklet(standingInstructionReadPlatformService,jdbcTemplate,sqlGenerator,accountTransfersWritePlatformService);
    }

    @AfterEach
    void tearDown() {
        executeStandingInstructionsTasklet = null;
    }



    /*tests a standing instruction with a due date in the past*/
    @org.junit.jupiter.api.Test
    public void standingInstructionWithPastDateShouldBeDue(){
        AccountTransferRecurrenceType recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES;
        StandingInstructionDuesData standingInstructionDuesData = new StandingInstructionDuesData(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()).minusDays(2),new BigDecimal("10000"));
        assertTrue(executeStandingInstructionsTasklet.isDueForTransfer(recurrenceType,standingInstructionDuesData));
    }

    /*tests a standing instruction due today*/
    @Test
    public void standingInstructionDueTodayShouldBeDue(){
        AccountTransferRecurrenceType recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES;
        StandingInstructionDuesData standingInstructionDuesData = new StandingInstructionDuesData(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()),new BigDecimal("10000"));
        assertTrue(executeStandingInstructionsTasklet.isDueForTransfer(recurrenceType,standingInstructionDuesData));
    }

    @Test
    public void standingInstructionDueInFutureShouldNotBeDue(){
        AccountTransferRecurrenceType recurrenceType = AccountTransferRecurrenceType.AS_PER_DUES;
        StandingInstructionDuesData standingInstructionDuesData = new StandingInstructionDuesData(LocalDate.now(DateUtils.getDateTimeZoneOfTenant()).plusDays(2),new BigDecimal("10000"));
        assertFalse(executeStandingInstructionsTasklet.isDueForTransfer(recurrenceType,standingInstructionDuesData));
    }
}