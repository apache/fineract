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
package org.apache.fineract.organisation.monetary.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CurrencyCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{org.apache.fineract.organisation.monetary.currencies.code.notBlank}")
    @Pattern(regexp = "^[A-Z]{3}$", message = "{org.apache.fineract.organisation.monetary" + ".currencies.code.invalidFormat}")
    private String code;

    @NotBlank(message = "{org.apache.fineract.organisation.monetary.currencies.name.notBlank}")
    @Size(min = 5, max = 50, message = "{org.apache.fineract.organisation.monetary.currencies" + ".name" + ".size}")
    private String name;

    @NotNull(message = "{org.apache.fineract.organisation.monetary.currencies.decimalPlaces" + ".notNull}")
    @Min(value = 0, message = "{org.apache.fineract.organisation.monetary.currencies" + ".decimalPlaces" + ".min}")
    @Max(value = 5, message = "{org.apache.fineract.organisation.monetary.currencies" + ".decimalPlaces" + ".max}")
    private Integer decimalPlaces;

    @PositiveOrZero(message = "{org.apache.fineract.organisation.monetary.currencies" + ".inMultiplesOf" + ".positiveOrZero}")
    private Integer inMultiplesOf;

    private String displaySymbol;

    @JsonIgnore
    private String nameCode;
}
