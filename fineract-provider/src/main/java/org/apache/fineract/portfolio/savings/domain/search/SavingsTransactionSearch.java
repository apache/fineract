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
package org.apache.fineract.portfolio.savings.domain.search;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import org.apache.fineract.infrastructure.core.data.RangeOperator;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;

@Data
public class SavingsTransactionSearch {

    private Filters filters;

    @Data
    public static class Filters {

        private List<RangeFilter<LocalDate>> transactionDate;

        private List<RangeFilter<BigDecimal>> transactionAmount;

        private List<SavingsAccountTransactionType> transactionType;
    }

    @Data
    public static class RangeFilter<T> {

        private RangeOperator operator;

        private T value;
    }

}
