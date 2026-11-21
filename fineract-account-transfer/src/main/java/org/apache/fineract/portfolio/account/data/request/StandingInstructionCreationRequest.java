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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.account.validation.ValidStandingInstructionCreate;

@Data
@NoArgsConstructor
@ValidStandingInstructionCreate
public class StandingInstructionCreationRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-office-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-office-id.positive}")
    private Long fromOfficeId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-client-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-client-id.positive}")
    private Long fromClientId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-account-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-account-id.positive}")
    private Long fromAccountId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.from-account-type.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.from-account-type.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.from-account-type.range}")
    private Integer fromAccountType;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-office-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-office-id.positive}")
    private Long toOfficeId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-client-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-client-id.positive}")
    private Long toClientId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-account-id.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-account-id.positive}")
    private Long toAccountId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.to-account-type.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.to-account-type.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.to-account-type.range}")
    private Integer toAccountType;

    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.name.not-null}")
    private String name;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.transfer-type.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.transfer-type.range}")
    @Max(value = 3, message = "{org.apache.fineract.portfolio.account.standinginstruction.transfer-type.range}")
    private Integer transferType;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.status.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.status.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.status.range}")
    private Integer status;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.priority.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.priority.range}")
    @Max(value = 4, message = "{org.apache.fineract.portfolio.account.standinginstruction.priority.range}")
    private Integer priority;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.instruction-type.not-null}")
    @Min(value = 1, message = "{org.apache.fineract.portfolio.account.standinginstruction.instruction-type.range}")
    @Max(value = 2, message = "{org.apache.fineract.portfolio.account.standinginstruction.instruction-type.range}")
    private Integer instructionType;
    @NotNull(message = "{org.apache.fineract.portfolio.account.standinginstruction.recurrence-type.not-null}")
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
    private Boolean allowPartialTransfer;

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
