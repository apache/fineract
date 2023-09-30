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
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.mapstruct.Mapper;

@Mapper(config = MapstructMapperConfig.class)
public interface OfficeSwaggerMapper {

    OfficesApiResourceSwagger.PutOfficesOfficeIdResponse toPutOfficesOfficeIdResponse(CommandProcessingResult commandProcessingResult);

    default OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges toPutOfficesOfficeIdResponseChanges(
            Map<String, Object> changes) {
        OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges response = new OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.PutOfficesOfficeIdResponseChanges();
        Optional.ofNullable(changes).map(c -> c.get("name")).ifPresent(c -> response.name = String.valueOf(c));
        return response;
    }

    OfficesApiResourceSwagger.GetOfficesResponse toGetOfficesResponse(OfficeData officeData);
}
