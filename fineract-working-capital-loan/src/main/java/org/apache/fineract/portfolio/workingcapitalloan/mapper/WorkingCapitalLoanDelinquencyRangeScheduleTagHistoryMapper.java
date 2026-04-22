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

package org.apache.fineract.portfolio.workingcapitalloan.mapper;

import java.util.List;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanDelinquencyTagHistoryData;
import org.apache.fineract.portfolio.workingcapitalloan.data.WorkingCapitalLoanRangeScheduleDelinquencyData;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDelinquencyRangeScheduleTagHistory;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapstructMapperConfig.class, uses = { DelinquencyRangeMapper.class })
public interface WorkingCapitalLoanDelinquencyRangeScheduleTagHistoryMapper {

    @Mapping(target = "loanId", source = "source.loan.id")
    @Mapping(target = "delinquentDays", ignore = true)
    @Mapping(target = "rangeId", source = "source.rangeSchedule.id")
    @Mapping(target = "periodNumber", source = "source.rangeSchedule.periodNumber")
    @Mapping(target = "delinquentAmount", source = "source.rangeSchedule.delinquentAmount")
    WorkingCapitalLoanDelinquencyTagHistoryData map(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory source);

    List<WorkingCapitalLoanDelinquencyTagHistoryData> map(List<WorkingCapitalLoanDelinquencyRangeScheduleTagHistory> sources);

    @AfterMapping
    default void calculateTotal(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory source,
            @MappingTarget WorkingCapitalLoanDelinquencyTagHistoryData target) {
        target.setDelinquentDays(source.getRangeSchedule().getDelinquentDays() - source.getDelinquencyRange().getMinimumAgeDays() + 1);
    }

    @Mapping(target = "rangeId", source = "delinquencyRange.id")
    @Mapping(target = "classification", source = "delinquencyRange.classification")
    @Mapping(target = "minimumAgeDays", source = "delinquencyRange.minimumAgeDays")
    @Mapping(target = "maximumAgeDays", source = "delinquencyRange.maximumAgeDays")
    @Mapping(target = "delinquentAmount", source = "outstandingAmount")
    WorkingCapitalLoanRangeScheduleDelinquencyData mapForCollectionData(WorkingCapitalLoanDelinquencyRangeScheduleTagHistory source);

}
