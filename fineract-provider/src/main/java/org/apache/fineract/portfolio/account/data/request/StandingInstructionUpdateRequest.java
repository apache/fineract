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
package org.apache.fineract.portfolio.account.data.request;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;

/**
 * Payload for updating an existing standing instruction. All fields are optional: only the values that are provided
 * (non-null) are applied, which is why the constraints below only bound the value ranges - a {@code null} always
 * passes.
 */
@Data
@NoArgsConstructor
public class StandingInstructionUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // set from the path parameter, never part of the request body
    @Hidden
    private Long id;

    private String name;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.status.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.status.range}")
    private Integer status;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.priority.range}")
    @Max(value = 4, message = "{org.apache.fineract.portfolio.account.standinginstruction.priority.range}")
    private Integer priority;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.instruction-type.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.instruction-type.range}")
    private Integer instructionType;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-type.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-type.range}")
    private Integer recurrenceType;
    @Min(value = 0, message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-frequency.range}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-frequency.range}")
    private Integer recurrenceFrequency;
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-interval.positive}")
    private Integer recurrenceInterval;
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.amount.positive}")
    private BigDecimal amount;

    private String validFrom;
    private String validTill;
    private String recurrenceOnMonthDay;
    private String dateFormat;
    private String monthDayFormat;
    private String locale;

    public LocalDate validFromAsDate() {
        return DateUtils.parseLocalDateOrNull(this.validFrom, this.dateFormat, this.locale);
    }

    public LocalDate validTillAsDate() {
        return DateUtils.parseLocalDateOrNull(this.validTill, this.dateFormat, this.locale);
    }

    public MonthDay recurrenceOnMonthDayAsMonthDay() {
        return DateUtils.parseMonthDay(this.recurrenceOnMonthDay, this.monthDayFormat, this.locale);
    }
}
