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
package org.apache.fineract.portfolio.floatingrates.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloatingRatePeriodRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.portfolio.floatingrate.from-date.not-blank}")
    private String fromDate;
    @NotNull(message = "{org.apache.fineract.portfolio.floatingrate.interest-rate.not-null}")
    @PositiveOrZero(message = "{org.apache.fineract.portfolio.floatingrate.interest-rate.positive-or-zero}")
    private BigDecimal interestRate;
    private Boolean isDifferentialToBaseLendingRate;
    private String locale;
    private String dateFormat;

    public LocalDate fromDateAsLocalDate() {
        if (StringUtils.isBlank(this.fromDate)) {
            return null;
        }
        if (StringUtils.isBlank(this.dateFormat)) {
            return LocalDate.parse(this.fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        final Locale parseLocale = StringUtils.isBlank(this.locale) ? Locale.getDefault()
                : Locale.forLanguageTag(this.locale.replace('_', '-'));
        final DateTimeFormatter formatter = new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(this.dateFormat)
                .toFormatter(parseLocale);
        return LocalDate.parse(this.fromDate, formatter);
    }
}
