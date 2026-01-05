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
package org.apache.fineract.infrastructure.businessdate.mapper;

import java.util.List;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateResponse;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.data.api.BusinessDateUpdateResponse;
import org.apache.fineract.infrastructure.businessdate.data.service.BusinessDateDTO;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(config = MapstructMapperConfig.class)
public interface BusinessDateMapper {

    @Mappings({ @Mapping(target = "description", source = "type.description"), @Mapping(target = "changes", ignore = true) })
    BusinessDateDTO mapEntity(BusinessDate source);

    List<BusinessDateDTO> mapEntity(List<BusinessDate> sources);

    @Mapping(target = "description", expression = "java(org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()).getDescription())")
    @Mapping(target = "type", expression = "java(org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()))")
    @Mapping(target = "date", expression = "java(org.apache.fineract.infrastructure.core.service.DateUtils.toLocalDate(source.getLocale(), source.getDate(), source.getDateFormat()))")
    @Mapping(target = "changes", ignore = true)
    BusinessDateDTO mapUpdateRequest(BusinessDateUpdateRequest source);

    List<BusinessDateResponse> mapFetchResponse(List<BusinessDateDTO> sources);

    BusinessDateResponse mapFetchResponse(BusinessDateDTO source);

    BusinessDateUpdateResponse mapUpdateResponse(BusinessDateDTO source);
}
