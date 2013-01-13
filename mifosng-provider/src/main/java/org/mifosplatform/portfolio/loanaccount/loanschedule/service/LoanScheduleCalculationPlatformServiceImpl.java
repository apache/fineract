package org.mifosplatform.portfolio.loanaccount.loanschedule.service;

import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.LoanSchedule;
import org.mifosplatform.portfolio.loanaccount.serialization.CalculateLoanScheduleQueryFromApiJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanScheduleCalculationPlatformServiceImpl implements LoanScheduleCalculationPlatformService {

    private final PlatformSecurityContext context;
    private final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanScheduleAssembler loanScheduleAssembler;

    @Autowired
    public LoanScheduleCalculationPlatformServiceImpl(final PlatformSecurityContext context,
            final CalculateLoanScheduleQueryFromApiJsonHelper fromApiJsonDeserializer,
            final LoanScheduleAssembler loanScheduleAssembler) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanScheduleAssembler = loanScheduleAssembler;
    }

    @Override
    public LoanScheduleData calculateLoanSchedule(final JsonQuery query) {
        context.authenticatedUser();

        this.fromApiJsonDeserializer.validate(query.json());

        final LoanSchedule loanSchedule = this.loanScheduleAssembler.fromJson(query.parsedJson());
        return loanSchedule.generate();
    }
}