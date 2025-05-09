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
package org.apache.fineract.portfolio.delinquency.data;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelinquencyRangeUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    @NotNull
    private Long id;

    @NotEmpty(message = "{org.apache.fineract.portfolio.delinquency.data.delinquency-range-request.classification.not-empty}")
    private String classification;

    @NotEmpty(message = "{org.apache.fineract.portfolio.delinquency.data.delinquency-range-request.minimumAgeDays.not-empty}")
    @PositiveOrZero(message = "{org.apache.fineract.portfolio.delinquency.data.delinquency-range-request.minimumagedays.positive}")
    private Integer minimumAgeDays;

    @PositiveOrZero(message = "{org.apache.fineract.portfolio.delinquency.data.delinquency-range-request.maximumagedays.positive}")
    private Integer maximumAgeDays;

    @NotEmpty(message = "{org.apache.fineract.portfolio.delinquency.data.delinquency-range-request.locale.not-empty}")
    private String locale;
}
