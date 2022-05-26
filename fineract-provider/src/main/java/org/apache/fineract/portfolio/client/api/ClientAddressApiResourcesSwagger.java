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
package org.apache.fineract.portfolio.client.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 01/12/18.
 */
@SuppressWarnings({ "MemberName" })
final class ClientAddressApiResourcesSwagger {

    private ClientAddressApiResourcesSwagger() {}

    @Schema(description = "PostClientClientIdAddressesRequest")
    public static final class PostClientClientIdAddressesRequest {

        private PostClientClientIdAddressesRequest() {}

        @Schema(example = "Ipca")
        public String street;
        @Schema(example = "Kandivali")
        public String addressLine1;
        @Schema(example = "plot47")
        public String addressLine2;
        @Schema(example = "charkop")
        public String addressLine3;
        @Schema(example = "Mumbai")
        public String city;
        @Schema(example = "800")
        public Integer stateProvinceId;
        @Schema(example = "802")
        public Integer countryId;
        @Schema(example = "400064")
        public Long postalCode;
        @Schema(example = "true")
        public Boolean isActive;
    }

    @Schema(description = "PostClientClientIdAddressesResponse")
    public static final class PostClientClientIdAddressesResponse {

        private PostClientClientIdAddressesResponse() {}

        @Schema(example = "15")
        public Integer resourceId;
    }

    @Schema(description = "GetClientClientIdAddressesResponse")
    public static final class GetClientClientIdAddressesResponse {

        private GetClientClientIdAddressesResponse() {}

        @Schema(example = "111755")
        public Long client_id;
        @Schema(example = "PERMANENT ADDRESS")
        public String addressType;
        @Schema(example = "14")
        public Integer addressId;
        @Schema(example = "804")
        public Integer addressTypeId;
        @Schema(example = "false")
        public Boolean isActive;
        @Schema(example = "anki's home")
        public String street;
        @Schema(example = "test123")
        public String addressLine1;
        @Schema(example = "iuyt")
        public String addressLine2;
        @Schema(example = " ")
        public String addressLine3;
        @Schema(example = " ")
        public String townVillage;
        @Schema(example = "mumbai")
        public String city;
        @Schema(example = " ")
        public String countyDistrict;
        @Schema(example = "801")
        public Integer stateProvinceId;
        @Schema(example = "UNITED STATES")
        public String countryName;
        @Schema(example = "GUJRAT")
        public String stateName;
        @Schema(example = "807")
        public Integer countryId;
        @Schema(example = "400095")
        public Long postalCode;
        @Schema(example = " ")
        public String createdBy;
        @Schema(example = " ")
        public String updatedBy;
    }

    @Schema(description = "PutClientClientIdAddressesRequest")
    public static final class PutClientClientIdAddressesRequest {

        private PutClientClientIdAddressesRequest() {}

        @Schema(example = "67")
        public Integer addressId;
        @Schema(example = "goldensource")
        public String street;
    }

    @Schema(description = "PutClientClientIdAddressesResponse")
    public static final class PutClientClientIdAddressesResponse {

        private PutClientClientIdAddressesResponse() {}

        @Schema(example = "67")
        public Integer resourceId;
    }
}
