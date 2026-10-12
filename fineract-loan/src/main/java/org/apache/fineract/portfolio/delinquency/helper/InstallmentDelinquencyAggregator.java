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
package org.apache.fineract.portfolio.delinquency.helper;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.loanaccount.data.InstallmentLevelDelinquency;

/**
 * Static utility class for aggregating installment-level delinquency data.
 *
 * @see InstallmentLevelDelinquency
 * @see LoanInstallmentDelinquencyTagData
 */
public final class InstallmentDelinquencyAggregator {

    private InstallmentDelinquencyAggregator() {}

    /**
     * Aggregates installment-level delinquency data by rangeId and sorts by minimumAgeDays.
     *
     * This method performs two key operations: 1. Groups installments by delinquency rangeId and sums delinquentAmount
     * for installments with the same rangeId 2. Sorts the aggregated results by minimumAgeDays in ascending order
     *
     * @param installmentData
     *            Collection of installment delinquency data to aggregate
     * @return Sorted list of aggregated delinquency data, empty list if input is null or empty
     */
    public static List<InstallmentLevelDelinquency> aggregateAndSort(Collection<LoanInstallmentDelinquencyTagData> installmentData) {

        if (installmentData == null || installmentData.isEmpty()) {
            return List.of();
        }

        Collection<InstallmentLevelDelinquency> aggregated = installmentData.stream().map(InstallmentLevelDelinquency::from)
                .collect(Collectors.groupingBy(InstallmentLevelDelinquency::getRangeId, delinquentAmountSummingCollector())).values()
                .stream().map(opt -> opt.orElseThrow(() -> new IllegalStateException("Unexpected empty Optional in aggregation"))).toList();

        return aggregated.stream().sorted(Comparator.comparing(ild -> Optional.ofNullable(ild.getMinimumAgeDays()).orElse(0))).toList();
    }

    /**
     * Creates a custom collector that sums delinquent amounts while preserving range metadata.
     *
     * This collector uses the reducing operation to combine multiple InstallmentLevelDelinquency objects with the same
     * rangeId. It preserves the range classification (rangeId, classification, minimumAgeDays, maximumAgeDays) while
     * summing the delinquentAmount fields.
     *
     * Note: This uses the 1-argument reducing() variant which returns Optional<T> to avoid the identity value bug that
     * would cause amounts to be incorrectly doubled when aggregating single installments.
     *
     * @return Collector that combines InstallmentLevelDelinquency objects by summing amounts
     */
    private static Collector<InstallmentLevelDelinquency, ?, Optional<InstallmentLevelDelinquency>> delinquentAmountSummingCollector() {
        return Collectors.reducing((item1, item2) -> {
            final InstallmentLevelDelinquency result = new InstallmentLevelDelinquency();
            result.setRangeId(item1.getRangeId());
            result.setClassification(item1.getClassification());
            result.setMaximumAgeDays(item1.getMaximumAgeDays());
            result.setMinimumAgeDays(item1.getMinimumAgeDays());
            result.setDelinquentAmount(MathUtil.add(item1.getDelinquentAmount(), item2.getDelinquentAmount()));
            return result;
        });
    }
}
