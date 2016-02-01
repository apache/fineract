/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accrual.service;

import static org.mifosplatform.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_EXECUTION_ERROR_CODE;
import static org.mifosplatform.accounting.accrual.api.AccrualAccountingConstants.PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME;
import static org.mifosplatform.accounting.accrual.api.AccrualAccountingConstants.accrueTillParamName;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.accrual.serialization.AccrualAccountingDataValidator;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccrualAccountingWritePlatformServiceImpl implements AccrualAccountingWritePlatformService {

    private final LoanAccrualPlatformService loanAccrualPlatformService;
    private final AccrualAccountingDataValidator accountingDataValidator;

    @Autowired
    public AccrualAccountingWritePlatformServiceImpl(final LoanAccrualPlatformService loanAccrualPlatformService,
            final AccrualAccountingDataValidator accountingDataValidator) {
        this.loanAccrualPlatformService = loanAccrualPlatformService;
        this.accountingDataValidator = accountingDataValidator;
    }

    @Override
    public CommandProcessingResult executeLoansPeriodicAccrual(JsonCommand command) {
        this.accountingDataValidator.validateLoanPeriodicAccrualData(command.json());
        LocalDate tilldate = command.localDateValueOfParameterNamed(accrueTillParamName);
        String errorlog = this.loanAccrualPlatformService.addPeriodicAccruals(tilldate);
        if (errorlog.length() > 0) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(PERIODIC_ACCRUAL_ACCOUNTING_RESOURCE_NAME);
            baseDataValidator.reset().failWithCodeNoParameterAddedToErrorCode(PERIODIC_ACCRUAL_ACCOUNTING_EXECUTION_ERROR_CODE, errorlog);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        return CommandProcessingResult.empty();
    }

}
