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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransferCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "{org.apache.fineract.portfolio.account.from-office-id.not-null}")
    private Long fromOfficeId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.from-client-id.not-null}")
    private Long fromClientId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.from-account-type.not-null}")
    private Integer fromAccountType;
    @NotNull(message = "{org.apache.fineract.portfolio.account.from-account-id.not-null}")
    private Long fromAccountId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.to-office-id.not-null}")
    private Long toOfficeId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.to-client-id.not-null}")
    private Long toClientId;
    @NotNull(message = "{org.apache.fineract.portfolio.account.to-account-type.not-null}")
    private Integer toAccountType;
    @NotNull(message = "{org.apache.fineract.portfolio.account.to-account-id.not-null}")
    private Long toAccountId;
    @NotBlank(message = "{org.apache.fineract.portfolio.account.transfer-date.not-blank}")
    private String transferDate;
    @NotNull(message = "{org.apache.fineract.portfolio.account.transfer-amount.not-null}")
    @Positive(message = "{org.apache.fineract.portfolio.account.transfer-amount.positive}")
    private BigDecimal transferAmount;
    @NotBlank(message = "{org.apache.fineract.portfolio.account.transfer-description.not-blank}")
    @Size(max = 200, message = "{org.apache.fineract.portfolio.account.transfer-description.size}")
    private String transferDescription;
    private String dateFormat;
    private String locale;
}
