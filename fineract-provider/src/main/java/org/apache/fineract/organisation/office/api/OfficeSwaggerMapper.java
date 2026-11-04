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
package org.apache.fineract.organisation.office.api;

import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.office.data.OfficeCreateResponse;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.data.OfficeUpdateResponse;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface OfficeSwaggerMapper {

    OfficesApiResourceSwagger.PostOfficesResponse toPostOfficesResponse(OfficeCreateResponse response);

    OfficesApiResourceSwagger.PutOfficesOfficeIdResponse toPutOfficesOfficeIdResponse(OfficeUpdateResponse response);

    default OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges toPutOfficesOfficeIdResponseChanges(
            Map<String, Object> changes) {
        if (changes == null) {
            return null;
        }
        OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges response = new OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges();
        Optional.ofNullable(changes.get("name")).ifPresent(c -> response.name = String.valueOf(c));
        Optional.ofNullable(changes.get("openingDate")).ifPresent(c -> response.openingDate = String.valueOf(c));
        Optional.ofNullable(changes.get("dateFormat")).ifPresent(c -> response.dateFormat = String.valueOf(c));
        Optional.ofNullable(changes.get("locale")).ifPresent(c -> response.locale = String.valueOf(c));
        Optional.ofNullable(changes.get("parentId"))
                .ifPresent(c -> response.parentId = c instanceof Number ? ((Number) c).longValue() : Long.parseLong(String.valueOf(c)));
        Optional.ofNullable(changes.get("externalId")).ifPresent(c -> response.externalId = String.valueOf(c));
        return response;
    }

    OfficesApiResourceSwagger.GetOfficesResponse toGetOfficesResponse(OfficeData officeData);
}
