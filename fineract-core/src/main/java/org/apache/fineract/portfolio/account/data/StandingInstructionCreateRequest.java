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
package org.apache.fineract.portfolio.account.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.validation.constraints.Locale;

@Getter
@Setter
@NoArgsConstructor
public class StandingInstructionCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.to.office.id.not.blank}")
    private String toOfficeId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.transfer.amount.not.blank}")
    private String amount;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.valid.till.not.blank}")
    private String validTill;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.to.account.type.not.blank}")
    private String toAccountType;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.date.format.not.blank}")
    private String dateFormat;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.recurrence.on.month.not.blank}")
    private String recurrenceOnMonthDay;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.to.account.id.not.blank}")
    private String toAccountId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.from.client.id.not.blank}")
    private String fromClientId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.valid.from.not.blank}")
    private String validFrom;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.locale.not.blank}")
    @Size(max = 50, message = "{org.apache.fineract.portfolio.account.data.locale.size}")
    @Locale
    private String locale;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.priority.not.blank}")
    private String priority;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.recurrence.type.not.blank}")
    private String recurrenceType;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.from.account.type.not.blank}")
    private String fromAccountType;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.recurrence.interval.not.blank}")
    private String recurrenceInterval;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.month.day.format.not.blank}")
    private String monthDayFormat;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.to.client.id.not.blank}")
    private String toClientId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.instruction.type.not.blank}")
    private String instructionType;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.from.account.id.not.blank}")
    private String fromAccountId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.recurrence.frequency.not.blank}")
    private String recurrenceFrequency;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.from.office.id.not.blank}")
    private String fromOfficeId;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.name.not.blank}")
    private String name;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.transfer.type.not.blank}")
    private String transferType;

    @NotBlank(message = "{org.apache.fineract.portfolio.account.data.status.not.blank}")
    private String status;
}
