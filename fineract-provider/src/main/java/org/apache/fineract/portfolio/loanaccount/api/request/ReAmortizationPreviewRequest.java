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
package org.apache.fineract.portfolio.loanaccount.api.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.QueryParam;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.reamortization.LoanReAmortizationInterestHandlingType;
import org.apache.fineract.validation.constraints.EnumValue;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReAmortizationPreviewRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @QueryParam("reAmortizationInterestHandling")
    @Parameter(description = "The interest handling type (DEFAULT, WAIVE_INTEREST, EQUAL_AMORTIZATION_INTEREST_SPLIT)", required = true)
    @NotBlank(message = "{org.apache.fineract.reamortization.interest-handling-type.not-blank}")
    @EnumValue(enumClass = LoanReAmortizationInterestHandlingType.class, message = "{org.apache.fineract.interest-handling-type.invalid}")
    private String reAmortizationInterestHandling;

}
