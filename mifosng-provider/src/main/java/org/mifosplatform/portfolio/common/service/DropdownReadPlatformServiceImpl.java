package org.mifosplatform.portfolio.common.service;

import static org.mifosplatform.portfolio.common.service.CommonEnumerations.conditionType;
import static org.mifosplatform.portfolio.common.service.CommonEnumerations.termFrequencyType;

import java.util.Arrays;
import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.common.domain.ConditionType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.stereotype.Service;

@Service
public class DropdownReadPlatformServiceImpl implements DropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrievePeriodFrequencyTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(termFrequencyType(PeriodFrequencyType.DAYS, "frequency"),
                termFrequencyType(PeriodFrequencyType.WEEKS, "frequency"), termFrequencyType(PeriodFrequencyType.MONTHS, "frequency"),
                termFrequencyType(PeriodFrequencyType.YEARS, "frequency"));
        return loanTermFrequencyOptions;
    }

    @Override
    public List<EnumOptionData> retrieveConditionTypeOptions() {
        final List<EnumOptionData> loanTermFrequencyOptions = Arrays.asList(conditionType(ConditionType.EQUAL, "condition"),
                conditionType(ConditionType.NOT_EQUAL, "condition"), conditionType(ConditionType.GRETERTHAN, "condition"),
                conditionType(ConditionType.LESSTHAN, "condition"));
        return loanTermFrequencyOptions;
    }

}
