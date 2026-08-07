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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StandingInstruction {

    private AccountTransferDetails accountTransferDetails;
    private String locale;
    private String dateFormat;
    private Integer transferType;
    private String name;
    private Integer priority;
    private Integer status;
    private Integer instructionType;
    private LocalDate validFrom;
    private LocalDate validTill;
    private BigDecimal amount;
    private Integer recurrenceType;
    private Integer recurrenceFrequency;
    private Integer recurrenceInterval;
    private String monthDayStr;
    private String monthDayFormat;
}