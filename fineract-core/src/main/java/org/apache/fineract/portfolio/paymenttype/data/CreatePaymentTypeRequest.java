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
package org.apache.fineract.portfolio.paymenttype.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.portfolio.paymenttype.validation.NotEmptyIfPresent;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CreatePaymentTypeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    @NotEmptyIfPresent
    private String name;

    @Size(max = 500, message = "{org.apache.fineract.portfolio.paymenttype.data.payment-type-response.description.length.max}")
    private String description;
    private Boolean isCashPayment;

    @PositiveOrZero(message = "{org.apache.fineract.portfolio.paymenttype.data.payment-type-response.position.positive}")
    private Integer position;

    @JsonProperty("code_name")
    @Size(max = 100, message = "{org.apache.fineract.portfolio.paymenttype.data.payment-type-response.codeName.length.max}")
    private String codeName;

    @JsonProperty("system_defined")
    private Boolean isSystemDefined;
}
