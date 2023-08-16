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
package org.apache.fineract.infrastructure.event.external.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.apache.fineract.infrastructure.event.external.exception.ExternalEventConfigurationNotFoundException;
import org.apache.fineract.infrastructure.event.external.service.validation.ExternalEventSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
public class ExternalEventConfigurationValidationServiceTest {

    @Mock
    private JdbcTemplateFactory jdbcTemplateFactory;

    @Mock
    private TenantDetailsService tenantDetailsService;

    @Mock
    private ExternalEventSourceService externalEventSourceService;

    private ExternalEventConfigurationValidationService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new ExternalEventConfigurationValidationService(tenantDetailsService, jdbcTemplateFactory, externalEventSourceService);
    }

    @Test
    public void givenAllConfigurationWhenValidatedThenValidationSuccessful() throws Exception {

        // given
        List<String> configurations = Arrays.asList("CentersCreateBusinessEvent", "ClientActivateBusinessEvent",
                "ClientCreateBusinessEvent", "ClientRejectBusinessEvent", "FixedDepositAccountCreateBusinessEvent",
                "GroupsCreateBusinessEvent", "LoanAcceptTransferBusinessEvent", "LoanAddChargeBusinessEvent",
                "LoanAdjustTransactionBusinessEvent", "LoanApplyOverdueChargeBusinessEvent", "LoanApprovedBusinessEvent",
                "LoanBalanceChangedBusinessEvent", "LoanChargebackTransactionBusinessEvent", "LoanChargePaymentPostBusinessEvent",
                "LoanChargePaymentPreBusinessEvent", "LoanChargeRefundBusinessEvent", "LoanCloseAsRescheduleBusinessEvent",
                "LoanCloseBusinessEvent", "LoanCreatedBusinessEvent", "LoanCreditBalanceRefundPostBusinessEvent",
                "LoanCreditBalanceRefundPreBusinessEvent", "LoanDeleteChargeBusinessEvent", "LoanDisbursalBusinessEvent",
                "LoanDisbursalTransactionBusinessEvent", "LoanForeClosurePostBusinessEvent", "LoanForeClosurePreBusinessEvent",
                "LoanInitiateTransferBusinessEvent", "LoanInterestRecalculationBusinessEvent", "LoanProductCreateBusinessEvent",
                "LoanReassignOfficerBusinessEvent", "LoanRefundPostBusinessEvent", "LoanRefundPreBusinessEvent",
                "LoanRejectedBusinessEvent", "LoanRejectTransferBusinessEvent", "LoanRemoveOfficerBusinessEvent",
                "LoanRepaymentDueBusinessEvent", "LoanRepaymentOverdueBusinessEvent", "LoanRescheduledDueCalendarChangeBusinessEvent",
                "LoanRescheduledDueHolidayBusinessEvent", "LoanScheduleVariationsAddedBusinessEvent",
                "LoanScheduleVariationsDeletedBusinessEvent", "LoanStatusChangedBusinessEvent",
                "LoanTransactionGoodwillCreditPostBusinessEvent", "LoanTransactionGoodwillCreditPreBusinessEvent",
                "LoanTransactionMakeRepaymentPostBusinessEvent", "LoanTransactionMakeRepaymentPreBusinessEvent",
                "LoanTransactionMerchantIssuedRefundPostBusinessEvent", "LoanTransactionMerchantIssuedRefundPreBusinessEvent",
                "LoanTransactionPayoutRefundPostBusinessEvent", "LoanTransactionPayoutRefundPreBusinessEvent",
                "LoanTransactionRecoveryPaymentPostBusinessEvent", "LoanTransactionRecoveryPaymentPreBusinessEvent",
                "LoanUndoApprovalBusinessEvent", "LoanUndoDisbursalBusinessEvent", "LoanUndoLastDisbursalBusinessEvent",
                "LoanUndoWrittenOffBusinessEvent", "LoanUpdateChargeBusinessEvent", "LoanUpdateDisbursementDataBusinessEvent",
                "LoanWaiveChargeBusinessEvent", "LoanWaiveChargeUndoBusinessEvent", "LoanWaiveInterestBusinessEvent",
                "LoanWithdrawTransferBusinessEvent", "LoanWrittenOffPostBusinessEvent", "LoanWrittenOffPreBusinessEvent",
                "RecurringDepositAccountCreateBusinessEvent", "SavingsActivateBusinessEvent", "SavingsApproveBusinessEvent",
                "SavingsCloseBusinessEvent", "SavingsCreateBusinessEvent", "SavingsDepositBusinessEvent",
                "SavingsPostInterestBusinessEvent", "SavingsRejectBusinessEvent", "SavingsWithdrawalBusinessEvent",
                "ShareAccountApproveBusinessEvent", "ShareAccountCreateBusinessEvent", "ShareProductDividentsCreateBusinessEvent",
                "LoanChargeAdjustmentPostBusinessEvent", "LoanChargeAdjustmentPreBusinessEvent", "LoanDelinquencyRangeChangeBusinessEvent",
                "LoanAccountsStayedLockedBusinessEvent", "MockBusinessEvent", "LoanChargeOffPreBusinessEvent",
                "LoanChargeOffPostBusinessEvent", "LoanUndoChargeOffBusinessEvent", "LoanAccrualTransactionCreatedBusinessEvent",
                "LoanRescheduledDueAdjustScheduleBusinessEvent", "LoanOwnershipTransferBusinessEvent", "LoanAccountSnapshotBusinessEvent",
                "LoanTransactionDownPaymentPostBusinessEvent", "LoanTransactionDownPaymentPreBusinessEvent");

        List<FineractPlatformTenant> tenants = Arrays
                .asList(new FineractPlatformTenant(1L, "default", "Default Tenant", "Europe/Budapest", null));

        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tenantDetailsService.findAllTenants()).thenReturn(tenants);
        when(jdbcTemplateFactory.create(any())).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(configurations);
        List<String> sourcePackage = Arrays.asList("org.apache.fineract");
        when(externalEventSourceService.getSourcePackages()).thenReturn(sourcePackage);
        // when
        underTest.afterPropertiesSet();

        // then
        verify(tenantDetailsService).findAllTenants();
        verify(jdbcTemplateFactory, times(1)).create(any());
    }

    @Test
    public void givenNoEventConfigurationWhenValidatedThenThrowException() throws Exception {
        // given
        List<FineractPlatformTenant> tenants = Arrays
                .asList(new FineractPlatformTenant(1L, "default", "Default Tenant", "Europe/Budapest", null));

        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tenantDetailsService.findAllTenants()).thenReturn(tenants);
        when(jdbcTemplateFactory.create(any())).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(new ArrayList<>());
        List<String> sourcePackage = Arrays.asList("org.apache.fineract");
        when(externalEventSourceService.getSourcePackages()).thenReturn(sourcePackage);
        // when
        ExternalEventConfigurationNotFoundException exceptionThrown = assertThrows(ExternalEventConfigurationNotFoundException.class,
                () -> underTest.afterPropertiesSet());

        // then
        String expectedMessage = "All external events are not configured";
        String actualMessage = exceptionThrown.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    public void givenMissingEventConfigurationWhenValidatedThenThrowException() throws Exception {

        // given
        List<String> configurationWithMissingCentersCreateBusinessEvent = Arrays.asList("MockBusinessEvent", "MockBusinessEvent",
                "ClientActivateBusinessEvent", "ClientCreateBusinessEvent", "ClientRejectBusinessEvent",
                "FixedDepositAccountCreateBusinessEvent", "GroupsCreateBusinessEvent", "LoanAcceptTransferBusinessEvent",
                "LoanAddChargeBusinessEvent", "LoanAdjustTransactionBusinessEvent", "LoanApplyOverdueChargeBusinessEvent",
                "LoanApprovedBusinessEvent", "LoanBalanceChangedBusinessEvent", "LoanChargebackTransactionBusinessEvent",
                "LoanChargePaymentPostBusinessEvent", "LoanChargePaymentPreBusinessEvent", "LoanChargeRefundBusinessEvent",
                "LoanCloseAsRescheduleBusinessEvent", "LoanCloseBusinessEvent", "LoanCreatedBusinessEvent",
                "LoanCreditBalanceRefundPostBusinessEvent", "LoanCreditBalanceRefundPreBusinessEvent", "LoanDeleteChargeBusinessEvent",
                "LoanDisbursalBusinessEvent", "LoanDisbursalTransactionBusinessEvent", "LoanForeClosurePostBusinessEvent",
                "LoanForeClosurePreBusinessEvent", "LoanInitiateTransferBusinessEvent", "LoanInterestRecalculationBusinessEvent",
                "LoanProductCreateBusinessEvent", "LoanReassignOfficerBusinessEvent", "LoanRefundPostBusinessEvent",
                "LoanRefundPreBusinessEvent", "LoanRejectedBusinessEvent", "LoanRejectTransferBusinessEvent",
                "LoanRemoveOfficerBusinessEvent", "LoanRepaymentDueBusinessEvent", "LoanRepaymentOverdueBusinessEvent",
                "LoanRescheduledDueCalendarChangeBusinessEvent", "LoanRescheduledDueHolidayBusinessEvent",
                "LoanScheduleVariationsAddedBusinessEvent", "LoanScheduleVariationsDeletedBusinessEvent", "LoanStatusChangedBusinessEvent",
                "LoanTransactionGoodwillCreditPostBusinessEvent", "LoanTransactionGoodwillCreditPreBusinessEvent",
                "LoanTransactionMakeRepaymentPostBusinessEvent", "LoanTransactionMakeRepaymentPreBusinessEvent",
                "LoanTransactionMerchantIssuedRefundPostBusinessEvent", "LoanTransactionMerchantIssuedRefundPreBusinessEvent",
                "LoanTransactionPayoutRefundPostBusinessEvent", "LoanTransactionPayoutRefundPreBusinessEvent",
                "LoanTransactionRecoveryPaymentPostBusinessEvent", "LoanTransactionRecoveryPaymentPreBusinessEvent",
                "LoanUndoApprovalBusinessEvent", "LoanUndoDisbursalBusinessEvent", "LoanUndoLastDisbursalBusinessEvent",
                "LoanUndoWrittenOffBusinessEvent", "LoanUpdateChargeBusinessEvent", "LoanUpdateDisbursementDataBusinessEvent",
                "LoanWaiveChargeBusinessEvent", "LoanWaiveChargeUndoBusinessEvent", "LoanWaiveInterestBusinessEvent",
                "LoanWithdrawTransferBusinessEvent", "LoanWrittenOffPostBusinessEvent", "LoanWrittenOffPreBusinessEvent",
                "RecurringDepositAccountCreateBusinessEvent", "SavingsActivateBusinessEvent", "SavingsApproveBusinessEvent",
                "SavingsCloseBusinessEvent", "SavingsCreateBusinessEvent", "SavingsDepositBusinessEvent",
                "SavingsPostInterestBusinessEvent", "SavingsRejectBusinessEvent", "SavingsWithdrawalBusinessEvent",
                "ShareAccountApproveBusinessEvent", "ShareAccountCreateBusinessEvent", "ShareProductDividentsCreateBusinessEvent",
                "LoanChargeAdjustmentPostBusinessEvent", "LoanChargeAdjustmentPreBusinessEvent", "LoanDelinquencyRangeChangeBusinessEvent",
                "LoanAccountsStayedLockedBusinessEvent", "LoanChargeOffPreBusinessEvent", "LoanChargeOffPostBusinessEvent",
                "LoanUndoChargeOffBusinessEvent", "LoanAccrualTransactionCreatedBusinessEvent",
                "LoanRescheduledDueAdjustScheduleBusinessEvent", "LoanOwnershipTransferBusinessEvent", "LoanAccountSnapshotBusinessEvent",
                "LoanTransactionDownPaymentPostBusinessEvent", "LoanTransactionDownPaymentPreBusinessEvent");

        List<FineractPlatformTenant> tenants = Arrays
                .asList(new FineractPlatformTenant(1L, "default", "Default Tenant", "Europe/Budapest", null));

        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(tenantDetailsService.findAllTenants()).thenReturn(tenants);
        when(jdbcTemplateFactory.create(any())).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(anyString(), eq(String.class))).thenReturn(configurationWithMissingCentersCreateBusinessEvent);
        List<String> sourcePackage = Arrays.asList("org.apache.fineract");
        when(externalEventSourceService.getSourcePackages()).thenReturn(sourcePackage);
        // when
        ExternalEventConfigurationNotFoundException exceptionThrown = assertThrows(ExternalEventConfigurationNotFoundException.class,
                () -> underTest.afterPropertiesSet());

        // then
        String expectedMessage = "Configuration not found for external event CentersCreateBusinessEvent";
        String actualMessage = exceptionThrown.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));

    }

}
