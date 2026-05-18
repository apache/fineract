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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TaxComponentUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotBlank(message = "{org.apache.fineract.portfolio.tax.component.name.not-empty}")
    private String name;
    @NotNull(message = "{org.apache.fineract.portfolio.tax.component.percentage.not-empty}")
    @Positive(message = "{org.apache.fineract.portfolio.tax.component.percentage.positive}")
    @DecimalMax(value = "100.00", message = "{org.apache.fineract.portfolio.tax.component.percentage.max}")
    private BigDecimal percentage;
    private String startDate;
    private String dateFormat;
    private String locale;

    @JsonIgnore
    @AssertTrue(message = "org.apache.fineract.portfolio.tax.component.start-date.valid")
    public boolean isStartDateValid() {
        if (startDate == null) {
            return true; // optional field
        }
        if (dateFormat == null) {
            return false; // need format to parse
        }
        try {
            var formatter = DateTimeFormatter.ofPattern(dateFormat);
            var parsedDate = LocalDate.parse(startDate, formatter);
            return parsedDate.isAfter(DateUtils.getBusinessLocalDate()); // strictly after
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
