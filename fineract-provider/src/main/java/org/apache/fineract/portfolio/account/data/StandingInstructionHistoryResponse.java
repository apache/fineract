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

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.client.data.ClientData;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandingInstructionHistoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long standingInstructionId;
    private String name;
    private OfficeData fromOffice;
    private ClientData fromClient;
    private EnumOptionData fromAccountType;
    private PortfolioAccountData fromAccount;
    private EnumOptionData toAccountType;
    private PortfolioAccountData toAccount;
    private OfficeData toOffice;
    private ClientData toClient;
    private BigDecimal amount;
    private String status;
    private LocalDate executionTime;
    private String errorLog;

}
