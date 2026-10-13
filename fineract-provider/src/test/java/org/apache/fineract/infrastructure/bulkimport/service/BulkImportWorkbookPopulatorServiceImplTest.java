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
package org.apache.fineract.infrastructure.bulkimport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.data.LookupMode;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BulkImportWorkbookPopulatorServiceImplTest {

    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final BulkImportWorkbookPopulatorServiceImpl service = new BulkImportWorkbookPopulatorServiceImpl(context,
            readService(OfficeReadPlatformService.class), readService(StaffReadService.class), readService(ClientReadPlatformService.class),
            readService(CenterReadPlatformService.class), readService(GroupReadPlatformService.class),
            readService(FundReadPlatformService.class), readService(PaymentTypeReadService.class),
            readService(LoanProductReadPlatformService.class),
            readService(org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService.class),
            readService(LoanReadPlatformService.class), readService(GLAccountReadPlatformService.class),
            readService(SavingsAccountReadPlatformService.class), readService(CodeValueReadPlatformService.class),
            readService(SavingsProductReadPlatformService.class), readService(ShareProductReadPlatformService.class),
            readService(ChargeReadPlatformService.class), readService(DepositProductReadPlatformService.class),
            readService(RoleReadPlatformService.class));

    // Every read service answers with no data, and an empty Page where it returns one.
    private static <T> T readService(final Class<T> type) {
        return mock(type, invocation -> invocation.getMethod().getReturnType() == Page.class ? new Page<>(List.of(), 0)
                : RETURNS_DEFAULTS.answer(invocation));
    }

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        when(context.authenticatedUser()).thenReturn(mock(AppUser.class));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    // The template download failed with an NPE deep inside the workbook populators' date formatting whenever dateFormat
    // was omitted, giving the user no feedback ("nothing happens" on click). Reject the request up front with a clear
    // validation error instead.
    @ParameterizedTest
    @EnumSource(value = GlobalEntityType.class, names = { "LOANS", "CLIENTS_PERSON", "CENTERS", "SAVINGS_ACCOUNT", "LOAN_TRANSACTIONS" })
    void missingDateFormatIsRejectedWithAClearValidationErrorForTemplatesThatFormatDates(GlobalEntityType entityType) {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.getTemplate(entityType.toString(), null, null, null, LookupMode.INCLUDE));

        assertEquals(1, exception.getErrors().size());
        assertEquals("dateFormat", exception.getErrors().get(0).getParameterName());
    }

    @Test
    void blankDateFormatIsRejectedWithAClearValidationError() {
        assertThrows(PlatformApiDataValidationException.class,
                () -> service.getTemplate(GlobalEntityType.LOANS.toString(), null, null, "   ", LookupMode.INCLUDE));
    }

    // These templates never format a date (they ignore dateFormat or only pass it to POI's createDateConstraint, which
    // accepts null), so they keep downloading without it.
    @ParameterizedTest
    @EnumSource(value = GlobalEntityType.class, names = { "OFFICES", "STAFF", "USERS", "CHART_OF_ACCOUNTS", "GL_JOURNAL_ENTRIES",
            "SHARE_ACCOUNTS" })
    void templatesThatDoNotFormatDatesDownloadWithoutDateFormat(GlobalEntityType entityType) {
        Response response = service.getTemplate(entityType.toString(), null, null, null, LookupMode.INCLUDE);

        assertEquals(200, response.getStatus());
    }
}
