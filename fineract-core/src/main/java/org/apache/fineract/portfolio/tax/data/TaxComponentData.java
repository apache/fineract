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
package org.apache.fineract.portfolio.tax.data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class TaxComponentData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private BigDecimal percentage;
    private EnumOptionData debitAccountType;
    private GLAccountData debitAccount;
    private EnumOptionData creditAccountType;
    private GLAccountData creditAccount;
    private LocalDate startDate;
    private Collection<TaxComponentHistoryData> taxComponentHistories;
    // template options
    private Map<String, List<GLAccountData>> glAccountOptions;
    private Collection<EnumOptionData> glAccountTypeOptions;

    public BigDecimal getApplicablePercentage(final LocalDate date) {
        if (DateUtils.isAfter(date, startDate)) {
            return getPercentage();
        } else {
            for (var componentHistory : this.taxComponentHistories) {
                if (DateUtils.isAfter(date, getStartDate())
                        && (componentHistory.getEndDate() == null || !DateUtils.isAfter(date, componentHistory.getEndDate()))) {
                    return componentHistory.getPercentage();
                }
            }
        }

        return null;
    }
}
