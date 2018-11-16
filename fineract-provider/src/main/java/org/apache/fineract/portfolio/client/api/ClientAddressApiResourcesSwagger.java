/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by Chirag Gupta on 01/12/18.
 */
final class ClientAddressApiResourcesSwagger {
    private ClientAddressApiResourcesSwagger() {
    }

    @ApiModel(value = "PostClientClientIdAddressesRequest")
    public final static class PostClientClientIdAddressesRequest {
        private PostClientClientIdAddressesRequest() {
        }

        @ApiModelProperty(example = "Ipca")
        public String street;
        @ApiModelProperty(example = "Kandivali")
        public String addressLine1;
        @ApiModelProperty(example = "plot47")
        public String addressLine2;
        @ApiModelProperty(example = "charkop")
        public String addressLine3;
        @ApiModelProperty(example = "Mumbai")
        public String city;
        @ApiModelProperty(example = "800")
        public Integer stateProvinceId;
        @ApiModelProperty(example = "802")
        public Integer countryId;
        @ApiModelProperty(example = "400064")
        public Long postalCode;
    }

    @ApiModel(value = "PostClientClientIdAddressesResponse")
    public final static class PostClientClientIdAddressesResponse {
        private PostClientClientIdAddressesResponse() {
        }

        @ApiModelProperty(example = "15")
        public Integer resourceId;
    }

    @ApiModel(value = "GetClientClientIdAddressesResponse")
    public final static class GetClientClientIdAddressesResponse {
        private GetClientClientIdAddressesResponse() {
        }

        @ApiModelProperty(example = "111755")
        public Long client_id;
        @ApiModelProperty(example = "PERMANENT ADDRESS")
        public String addressType;
        @ApiModelProperty(example = "14")
        public Integer addressId;
        @ApiModelProperty(example = "804")
        public Integer addressTypeId;
        @ApiModelProperty(example = "false")
        public Boolean isActive;
        @ApiModelProperty(example = "anki's home")
        public String street;
        @ApiModelProperty(example = "test123")
        public String addressLine1;
        @ApiModelProperty(example = "iuyt")
        public String addressLine2;
        @ApiModelProperty(example = " ")
        public String addressLine3;
        @ApiModelProperty(example = " ")
        public String townVillage;
        @ApiModelProperty(example = "mumbai")
        public String city;
        @ApiModelProperty(example = " ")
        public String countyDistrict;
        @ApiModelProperty(example = "801")
        public Integer stateProvinceId;
        @ApiModelProperty(example = "UNITED STATES")
        public String countryName;
        @ApiModelProperty(example = "GUJRAT")
        public String stateName;
        @ApiModelProperty(example = "807")
        public Integer countryId;
        @ApiModelProperty(example = "400095")
        public Long postalCode;
        @ApiModelProperty(example = " ")
        public String createdBy;
        @ApiModelProperty(example = " ")
        public String updatedBy;
    }

    @ApiModel(value = "PutClientClientIdAddressesRequest")
    public final static class PutClientClientIdAddressesRequest {
        private PutClientClientIdAddressesRequest() {
        }

        @ApiModelProperty(example = "67")
        public Integer addressId;
        @ApiModelProperty(example = "goldensource")
        public String street;
    }

    @ApiModel(value = "PutClientClientIdAddressesResponse")
    public final static class PutClientClientIdAddressesResponse {
        private PutClientClientIdAddressesResponse() {
        }

        @ApiModelProperty(example = "67")
        public Integer resourceId;
    }
}
