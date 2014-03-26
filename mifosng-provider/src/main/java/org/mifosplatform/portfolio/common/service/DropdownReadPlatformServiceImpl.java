package org.mifosplatform.portfolio.common.service;

import static org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations.loanTermFrequencyType;

import java.util.Arrays;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class DropdownReadPlatformServiceImpl implements DropdownReadPlatformService{

    @Override
    public List<EnumOptionData> retrievePeriodFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(loanTermFrequencyType(PeriodFrequencyType.DAYS),
                loanTermFrequencyType(PeriodFrequencyType.WEEKS), loanTermFrequencyType(PeriodFrequencyType.MONTHS),
                loanTermFrequencyType(PeriodFrequencyType.YEARS));
        return loanTermFrequencyOptions;
    }

}
