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
package org.apache.fineract.portfolio.address.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.experimental.FieldNameConstants;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class AddressCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @NotNull(message = "{org.apache.fineract.portfolio.address.create.assertion.client-id-required}")
    @Positive(message = "{org.apache.fineract.portfolio.address.create.assertion.client-id-positive}")
    private Long clientId;

    @JsonIgnore
    @NotNull(message = "{org.apache.fineract.portfolio.address.create.assertion.address-type-id-required}")
    @Positive(message = "{org.apache.fineract.portfolio.address.create.assertion.address-type-id-positive}")
    private Long addressTypeId;

    @Size(max = 100)
    private String city;

    private Long countryId;

    private Boolean isActive;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String addressLine1;

    @Size(max = 100)
    private String addressLine2;

    @Size(max = 100)
    private String addressLine3;

    @Size(max = 100)
    private String townVillage;

    @Size(max = 100)
    private String countyDistrict;

    private Long stateProvinceId;

    private BigDecimal latitude;

    private BigDecimal longitude;
}
