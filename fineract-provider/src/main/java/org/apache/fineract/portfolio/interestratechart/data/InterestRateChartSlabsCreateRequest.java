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

package org.apache.fineract.portfolio.interestratechart.data;

import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InterestRateChartSlabsCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long chartId;

    private Long chartSlabId;

    private Integer periodType;

    private Integer fromPeriod;

    private Integer toPeriod;

    private BigDecimal amountRangeFrom;

    private BigDecimal amountRangeTo;

    private Double annualInterestRate;

    private String currencyCode;

    private String description;

    private String locale;

    private List<Incentive> incentives;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class Incentive implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;

        private String description;

        private Integer entityType;

        private Integer attributeName;

        private Integer conditionType;

        private String attributeValue;

        private Integer incentiveType;

        private BigDecimal amount;
    }
}
