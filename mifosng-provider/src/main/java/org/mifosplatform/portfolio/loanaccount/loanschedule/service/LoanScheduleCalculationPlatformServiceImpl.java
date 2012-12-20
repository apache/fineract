package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanSchedule;
import org.mifosplatform.portfolio.loanaccount.loanschedule.query.CalculateLoanScheduleQuery;
import org.mifosplatform.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

    private final PlatformSecurityContext context;
    private final CalculateLoanScheduleQueryFromApiJsonDeserializer fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;

    @Autowired
    public LoanScheduleCalculationPlatformServiceImpl(final PlatformSecurityContext context,
            final CalculateLoanScheduleQueryFromApiJsonDeserializer fromApiJsonDeserializer,
            final LoanScheduleAssembler loanScheduleAssembler) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanScheduleAssembler = loanScheduleAssembler;
    }

    @Override
    public LoanScheduleData calculateLoanSchedule(final JsonQuery query) {
        context.authenticatedUser();

        final CalculateLoanScheduleQuery calculateLoanScheduleQuery = this.fromApiJsonDeserializer.commandFromApiJson(query.json());
        final List<ApiParameterError> dataValidationErrors = calculateLoanScheduleQuery.validate();
        
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors); 
        }

        final LoanSchedule loanSchedule = this.loanScheduleAssembler.fromJson(query.parsedJson());
        return loanSchedule.generate();
    }
}