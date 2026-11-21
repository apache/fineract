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

/**
 * Creates one address for a client, either through {@code POST /v1/client/{clientId}/addresses} or as an entry of the
 * {@code address} array of the client creation request.
 *
 * The size limits follow the {@code m_address} columns. The REST endpoint keeps documenting
 * {@link org.apache.fineract.portfolio.client.data.ClientAddressRequest} as its request body, so these constraints do
 * not change the published API contract.
 */
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

    // Read from the body for the client creation request; the REST endpoint overwrites it with the "type" query
    // parameter.
    @NotNull(message = "{org.apache.fineract.portfolio.address.create.assertion.address-type-id-required}")
    @Positive(message = "{org.apache.fineract.portfolio.address.create.assertion.address-type-id-positive}")
    private Long addressTypeId;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.street-max-length}")
    private String street;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.address-line1-max-length}")
    private String addressLine1;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.address-line2-max-length}")
    private String addressLine2;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.address-line3-max-length}")
    private String addressLine3;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.town-village-max-length}")
    private String townVillage;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.city-max-length}")
    private String city;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.county-district-max-length}")
    private String countyDistrict;

    private Long stateProvinceId;

    private Long countryId;

    @Size(max = 10, message = "{org.apache.fineract.portfolio.address.create.assertion.postal-code-max-length}")
    private String postalCode;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Boolean isActive;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.created-by-max-length}")
    private String createdBy;

    @Size(max = 100, message = "{org.apache.fineract.portfolio.address.create.assertion.updated-by-max-length}")
    private String updatedBy;
}
