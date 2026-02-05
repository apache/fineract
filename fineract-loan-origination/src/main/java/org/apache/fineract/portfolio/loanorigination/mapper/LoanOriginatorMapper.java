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
package org.apache.fineract.portfolio.loanorigination.mapper;

import java.util.List;
import org.apache.fineract.infrastructure.codes.mapper.CodeValueMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.loanorigination.data.LoanOriginatorData;
import org.apache.fineract.portfolio.loanorigination.domain.LoanOriginator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Mapper(config = MapstructMapperConfig.class, uses = { CodeValueMapper.class })
@ConditionalOnProperty(value = "fineract.module.loan-origination.enabled", havingValue = "true")
public interface LoanOriginatorMapper {

    @Mapping(target = "originatorType", source = "originatorType")
    @Mapping(target = "channelType", source = "channelType")
    @Mapping(target = "externalId", source = "externalId")
    @Mapping(target = "status", expression = "java(entity.getStatus().getValue())")
    LoanOriginatorData toData(LoanOriginator entity);

    List<LoanOriginatorData> toDataList(List<LoanOriginator> entities);
}
