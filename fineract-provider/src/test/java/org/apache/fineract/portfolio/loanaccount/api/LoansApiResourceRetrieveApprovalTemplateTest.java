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
package org.apache.fineract.portfolio.loanaccount.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanApprovedAmountHistoryRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import org.apache.fineract.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanSummaryProviderDelegate;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Exercises the <strong>public</strong> API
 * {@link LoansApiResource#retrieveApprovalTemplate(Long, String, jakarta.ws.rs.core.UriInfo)} — the HTTP-facing method
 * with parameters {@code (loanId, templateType, uriInfo)}. That method forwards to a <strong>private</strong> overload
 * {@code retrieveApprovalTemplate(Long, String, String, UriInfo)} which adds {@code loanExternalIdStr} (always
 * {@code null} for this entry point).
 * <p>
 * Only {@link PlatformSecurityContext} and {@link LoanReadPlatformService} are Mockito mocks under test control; other
 * constructor dependencies are plain {@code mock(...)} placeholders (same pattern as) . Each {@code import} in this
 * file matches a concrete type passed to {@code mock(...)} in {@link #newLoansApiResource}; all are required for
 * compilation (no looser shared type for those dependencies).
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LoansApiResourceRetrieveApprovalTemplateTest {

    /**
     * Must match the permission resource name used inside {@link LoansApiResource} for loan read operations (field
     * {@code RESOURCE_NAME_FOR_PERMISSIONS}, value {@code "LOAN"}). Fineract does not expose that field publicly; keep
     * this in sync if the production constant ever changes.
     */
    private static final String LOAN_READ_PERMISSION_RESOURCE = "LOAN";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private AppUser appUser;

    private LoansApiResource loansApiResource;

    @BeforeEach
    void setUp() {
        loansApiResource = newLoansApiResource(context, loanReadPlatformService);
        when(context.authenticatedUser()).thenReturn(appUser);
        doNothing().when(appUser).validateHasReadPermission(LOAN_READ_PERMISSION_RESOURCE);
    }

    @Test
    void retrieveApprovalTemplate_unsupportedTemplateType_throwsNotSupportedLoanTemplateTypeException() {

        UriInfo uriInfo = mock(UriInfo.class);

        assertThrows(NotSupportedLoanTemplateTypeException.class,
                () -> loansApiResource.retrieveApprovalTemplate(1L, "unsupportedType", uriInfo));

        verify(appUser).validateHasReadPermission(LOAN_READ_PERMISSION_RESOURCE);
    }

    private static LoansApiResource newLoansApiResource(PlatformSecurityContext context, LoanReadPlatformService loanReadPlatformService) {

        ApiRequestParameterHelper helper = mock(ApiRequestParameterHelper.class);

        return new LoansApiResource(context, loanReadPlatformService, mock(LoanProductReadPlatformService.class),
                mock(LoanDropdownReadPlatformService.class), mock(FundReadPlatformService.class), mock(ChargeReadPlatformService.class),
                mock(LoanChargeReadPlatformService.class), mock(LoanScheduleCalculationPlatformService.class),
                mock(GuarantorReadPlatformService.class), mock(CodeValueReadPlatformService.class), mock(GroupReadPlatformService.class),
                mock(DefaultToApiJsonSerializer.class), mock(DefaultToApiJsonSerializer.class), mock(DefaultToApiJsonSerializer.class),
                mock(DefaultToApiJsonSerializer.class), helper, mock(FromJsonHelper.class),
                mock(PortfolioCommandSourceWritePlatformService.class), mock(CalendarReadPlatformService.class),
                mock(NoteReadPlatformService.class), mock(PortfolioAccountReadPlatformService.class),
                mock(AccountAssociationsReadPlatformService.class), mock(LoanScheduleHistoryReadPlatformService.class),
                mock(AccountDetailsReadPlatformService.class), mock(EntityDatatableChecksReadService.class),
                mock(BulkImportWorkbookService.class), mock(BulkImportWorkbookPopulatorService.class), mock(RateReadService.class),
                mock(ConfigurationDomainService.class), mock(DefaultToApiJsonSerializer.class),
                mock(GLIMAccountInfoReadPlatformService.class), mock(LoanCollateralManagementReadPlatformService.class),
                mock(DefaultToApiJsonSerializer.class), mock(DelinquencyReadPlatformService.class), mock(SqlValidator.class),
                mock(LoanSummaryBalancesRepository.class), mock(ClientReadPlatformService.class), mock(LoanTermVariationsRepository.class),
                mock(LoanSummaryProviderDelegate.class), mock(LoanCapitalizedIncomeBalanceRepository.class),
                mock(LoanApprovedAmountHistoryRepository.class), Optional.empty());
    }
}
