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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class TaxComponentCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.portfolio.tax.component.name.not-empty}")
    private String name;
    @NotNull(message = "{org.apache.fineract.portfolio.tax.component.percentage.not-empty}")
    @Positive(message = "{org.apache.fineract.portfolio.tax.component.percentage.positive}")
    @DecimalMax(value = "100.00", message = "{org.apache.fineract.portfolio.tax.component.percentage.max}")
    private BigDecimal percentage;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.tax.component.debit-account-type.min}")
    @Max(value = 5, message = "{org.apache.fineract.portfolio.tax.component.debit-account-type.max}")
    private Integer debitAccountType;
    @Positive(message = "{org.apache.fineract.portfolio.tax.component.debit-account-id.positive}")
    private Long debitAccountId;
    @Min(value = 1, message = "{org.apache.fineract.portfolio.tax.component.credit-account-type.min}")
    @Max(value = 5, message = "{org.apache.fineract.portfolio.tax.component.credit-account-type.max}")
    private Integer creditAccountType;
    @Positive(message = "{org.apache.fineract.portfolio.tax.component.credit-account-id.positive}")
    private Long creditAccountId;
    private String startDate;
    private String dateFormat;
    private String locale;

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.portfolio.tax.component.debit-account.valid}")
    public boolean isDebitAccountPairValid() {
        boolean typePresent = debitAccountType != null;
        boolean idPresent = debitAccountId != null;
        return typePresent == idPresent; // both or neither
    }

    @JsonIgnore
    @AssertTrue(message = "{org.apache.fineract.portfolio.tax.component.debit-account.valid}")
    public boolean isCreditAccountPairValid() {
        boolean typePresent = creditAccountType != null;
        boolean idPresent = creditAccountId != null;
        return typePresent == idPresent;
    }
}
