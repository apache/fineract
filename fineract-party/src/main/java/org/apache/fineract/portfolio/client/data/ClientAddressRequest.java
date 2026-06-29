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
package org.apache.fineract.portfolio.client.data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClientAddressRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String city;
    private Long countryId;
    private Boolean isActive;
    private String postalCode;
    private Long addressTypeId;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String townVillage;
    private String countyDistrict;
    private Long stateProvinceId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String createdBy;
    private String createdOn;
    private String updatedBy;
    private String updatedOn;
    private Long addressId;
}
