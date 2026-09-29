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
import static org.mockito.Mockito.mock;

import org.apache.fineract.accounting.glaccount.service.GLAccountReadPlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.data.LookupMode;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
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
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.junit.jupiter.api.Test;

class BulkImportWorkbookPopulatorServiceImplTest {

    private final BulkImportWorkbookPopulatorServiceImpl service = new BulkImportWorkbookPopulatorServiceImpl(
            mock(PlatformSecurityContext.class), mock(OfficeReadPlatformService.class), mock(StaffReadService.class),
            mock(ClientReadPlatformService.class), mock(CenterReadPlatformService.class), mock(GroupReadPlatformService.class),
            mock(FundReadPlatformService.class), mock(PaymentTypeReadService.class), mock(LoanProductReadPlatformService.class),
            mock(org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService.class), mock(LoanReadPlatformService.class),
            mock(GLAccountReadPlatformService.class), mock(SavingsAccountReadPlatformService.class),
            mock(CodeValueReadPlatformService.class), mock(SavingsProductReadPlatformService.class),
            mock(ShareProductReadPlatformService.class), mock(ChargeReadPlatformService.class),
            mock(DepositProductReadPlatformService.class), mock(RoleReadPlatformService.class));

    // The template download failed with an NPE deep inside the workbook populators' date formatting whenever dateFormat
    // was omitted, giving the user no feedback ("nothing happens" on click). Reject the request up front with a clear
    // validation error instead.
    @Test
    void missingDateFormatIsRejectedWithAClearValidationError() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> service.getTemplate(GlobalEntityType.LOANS.toString(), null, null, null, LookupMode.INCLUDE));

        assertEquals(1, exception.getErrors().size());
        assertEquals("dateFormat", exception.getErrors().get(0).getParameterName());
    }

    @Test
    void blankDateFormatIsRejectedWithAClearValidationError() {
        assertThrows(PlatformApiDataValidationException.class,
                () -> service.getTemplate(GlobalEntityType.LOANS.toString(), null, null, "   ", LookupMode.INCLUDE));
    }
}
