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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.AccountTransfersWritePlatformService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifies how {@link ExecuteStandingInstructionsTasklet} records each run in
 * m_account_transfer_standing_instructions_history: the values are bound as parameters, the amount keeps its BigDecimal
 * precision and the error log never exceeds the column size.
 */
@ExtendWith(MockitoExtension.class)
public class ExecuteStandingInstructionsTaskletTest {

    private static final String EXPECTED_INSERT = "INSERT INTO m_account_transfer_standing_instructions_history (standing_instruction_id, "
            + "status, amount, execution_time, error_log) VALUES (?, ?, ?, now(), ?)";
    private static final Long INSTRUCTION_ID = 7L;
    private static final BigDecimal AMOUNT = new BigDecimal("123.456789");

    private final LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());

    @Mock
    private StandingInstructionReadPlatformService standingInstructionReadPlatformService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private DatabaseSpecificSQLGenerator sqlGenerator;
    @Mock
    private AccountTransfersWritePlatformService accountTransfersWritePlatformService;

    private ExecuteStandingInstructionsTasklet tasklet;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Africa/Kampala", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        when(sqlGenerator.escape("status")).thenReturn("status");
        when(standingInstructionReadPlatformService.retrieveAll(StandingInstructionStatus.ACTIVE.getValue()))
                .thenReturn(List.of(dueSavingsToSavingsInstruction()));
        tasklet = new ExecuteStandingInstructionsTasklet(standingInstructionReadPlatformService, jdbcTemplate, sqlGenerator,
                accountTransfersWritePlatformService);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void failedTransferMessageWithQuoteIsBoundAsParameter() throws Exception {
        doThrow(new IllegalStateException("Account 'SAV-01' can't be debited")).when(accountTransfersWritePlatformService)
                .transferFunds(any(AccountTransferDTO.class));

        assertThrows(JobExecutionException.class, () -> tasklet.execute(null, null));

        verify(jdbcTemplate).update(EXPECTED_INSERT, INSTRUCTION_ID, "failed", AMOUNT,
                "Exception while trasfering funds Account 'SAV-01' can't be debited");
    }

    @Test
    public void failedTransferMessageLongerThanColumnIsTruncated() throws Exception {
        String longMessage = "x".repeat(ExecuteStandingInstructionsTasklet.ERROR_LOG_MAX_LENGTH + 100);
        doThrow(new IllegalStateException(longMessage)).when(accountTransfersWritePlatformService)
                .transferFunds(any(AccountTransferDTO.class));

        assertThrows(JobExecutionException.class, () -> tasklet.execute(null, null));

        String expectedErrorLog = ("Exception while trasfering funds " + longMessage).substring(0,
                ExecuteStandingInstructionsTasklet.ERROR_LOG_MAX_LENGTH);
        verify(jdbcTemplate).update(EXPECTED_INSERT, INSTRUCTION_ID, "failed", AMOUNT, expectedErrorLog);
    }

    @Test
    public void successfulTransferIsRecordedWithExactAmountAndEmptyErrorLog() throws Exception {
        tasklet.execute(null, null);

        verify(jdbcTemplate).update(EXPECTED_INSERT, INSTRUCTION_ID, "success", AMOUNT, "");
        verify(jdbcTemplate).update("UPDATE m_account_transfer_standing_instructions SET last_run_date = ? where id = ?", businessDate,
                INSTRUCTION_ID);
    }

    /**
     * A weekly savings-to-savings instruction whose first occurrence is the business date, so it is due today.
     */
    private StandingInstructionData dueSavingsToSavingsInstruction() {
        EnumOptionData savings = option(PortfolioAccountType.SAVINGS.getValue(), PortfolioAccountType.SAVINGS.getCode());
        return StandingInstructionData.instance(INSTRUCTION_ID, null, "Weekly savings", null, null, null, null, savings,
                PortfolioAccountData.lookup(10L, "from"), savings, PortfolioAccountData.lookup(20L, "to"),
                option(AccountTransferType.ACCOUNT_TRANSFER.getValue(), AccountTransferType.ACCOUNT_TRANSFER.getCode()), null,
                option(StandingInstructionType.FIXED.getValue(), StandingInstructionType.FIXED.getCode()), null, AMOUNT, businessDate,
                businessDate.plusYears(1),
                option(AccountTransferRecurrenceType.PERIODIC.getValue(), AccountTransferRecurrenceType.PERIODIC.getCode()),
                option(PeriodFrequencyType.WEEKS.getValue(), PeriodFrequencyType.WEEKS.getCode()), 1, MonthDay.from(businessDate));
    }

    private static EnumOptionData option(final Integer id, final String code) {
        return new EnumOptionData(id.longValue(), code, code);
    }
}
