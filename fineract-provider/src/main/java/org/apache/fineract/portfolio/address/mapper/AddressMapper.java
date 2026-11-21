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
package org.apache.fineract.portfolio.address.mapper;

import org.apache.fineract.portfolio.address.data.AddressCreateRequest;
import org.apache.fineract.portfolio.address.data.AddressUpdateRequest;
import org.apache.fineract.portfolio.address.domain.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    // Missing text fields are stored as "" and missing coordinates as 0, as the legacy create path did.
    @Mapping(target = "street", defaultExpression = "java(\"\")")
    @Mapping(target = "addressLine1", defaultExpression = "java(\"\")")
    @Mapping(target = "addressLine2", defaultExpression = "java(\"\")")
    @Mapping(target = "addressLine3", defaultExpression = "java(\"\")")
    @Mapping(target = "townVillage", defaultExpression = "java(\"\")")
    @Mapping(target = "city", defaultExpression = "java(\"\")")
    @Mapping(target = "countyDistrict", defaultExpression = "java(\"\")")
    @Mapping(target = "postalCode", defaultExpression = "java(\"\")")
    @Mapping(target = "createdBy", defaultExpression = "java(\"\")")
    @Mapping(target = "updatedBy", defaultExpression = "java(\"\")")
    @Mapping(target = "latitude", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "longitude", defaultExpression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientaddress", ignore = true)
    @Mapping(target = "stateProvince", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    Address toAddress(AddressCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clientaddress", ignore = true)
    @Mapping(target = "stateProvince", ignore = true)
    @Mapping(target = "country", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    // Fields that are not sent (null) keep their stored value.
    void updateAddressFromRequest(AddressUpdateRequest request, @MappingTarget Address address);
}
