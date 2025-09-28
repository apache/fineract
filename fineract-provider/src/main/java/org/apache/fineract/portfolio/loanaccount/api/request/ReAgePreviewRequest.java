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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.QueryParam;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.validation.constraints.EnumValue;
import org.apache.fineract.validation.constraints.LocalDate;
import org.apache.fineract.validation.constraints.Locale;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@LocalDate(dateField = "startDate", formatField = "dateFormat", localeField = "locale")
public class ReAgePreviewRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @QueryParam("frequencyNumber")
    @Parameter(description = "The frequency number for the re-aging schedule", required = true)
    @NotNull(message = "{org.apache.fineract.reage.frequency-number.not-blank}")
    @Min(value = 1, message = "{org.apache.fineract.reage.frequency-number.min}")
    private Integer frequencyNumber;

    @QueryParam("frequencyType")
    @Parameter(description = "The frequency type (DAYS, WEEKS, MONTHS, YEARS)", required = true)
    @NotBlank(message = "{org.apache.fineract.reage.frequency-type.not-blank}")
    @EnumValue(enumClass = PeriodFrequencyType.class, message = "{org.apache.fineract.frequency-type.invalid}")
    private String frequencyType;

    @QueryParam("startDate")
    @Parameter(description = "The start date for the re-aging schedule", required = true)
    @NotBlank(message = "{org.apache.fineract.reage.start-date.not-blank}")
    private String startDate;

    @QueryParam("numberOfInstallments")
    @Parameter(description = "The number of installments for the re-aged loan", required = true)
    @NotNull(message = "{org.apache.fineract.reage.number-of-installments.not-blank}")
    @Min(value = 1, message = "{org.apache.fineract.reage.number-of-installments.min}")
    private Integer numberOfInstallments;

    @QueryParam("dateFormat")
    @Parameter(description = "The date format used for the startDate parameter", required = true)
    @NotBlank(message = "{org.apache.fineract.businessdate.date-format.not-blank}")
    private String dateFormat;

    @QueryParam("locale")
    @Parameter(description = "The locale to use for formatting", required = true)
    @NotBlank(message = "{org.apache.fineract.businessdate.locale.not-blank}")
    @Locale
    private String locale;

}
