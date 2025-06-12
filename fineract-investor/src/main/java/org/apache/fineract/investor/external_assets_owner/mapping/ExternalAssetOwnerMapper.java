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
package org.apache.fineract.investor.external_assets_owner.mapping;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.external_assets_owner.data.ExternalAssetOwnerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface ExternalAssetOwnerMapper {

    @Mappings({ @Mapping(target = "resourceId", source = "id"),
            @Mapping(target = "resourceExternalId", source = "externalId", qualifiedByName = "mapExternalId"),
            @Mapping(target = "subResourceId", source = "loanId"),
            @Mapping(target = "subResourceExternalId", source = "externalLoanId", qualifiedByName = "mapExternalLoanId") })
    ExternalAssetOwnerResponse map(ExternalAssetOwnerTransfer source);

    @Named("mapExternalId")
    default String mapExternalId(ExternalId externalId) {
        return externalId != null ? externalId.getValue() : null;
    }

    @Named("mapExternalLoanId")
    default String mapExternalLoanId(ExternalId externalLoanId) {
        return externalLoanId != null ? externalLoanId.getValue() : null;
    }
}
