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
package org.apache.fineract.portfolio.tax.mapper;

import java.util.List;
import org.apache.fineract.accounting.glaccount.mapper.GlAccountMapper;
import org.apache.fineract.accounting.glaccount.mapper.GlAccountTypeMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = { GlAccountMapper.class, GlAccountTypeMapper.class })
public interface TaxComponentMapper {

    @Mapping(target = "creditAccount", source = "taxComponent.creditAccount")
    @Mapping(target = "debitAccount", source = "taxComponent.debitAccount")
    @Mapping(target = "creditAccountType", source = "taxComponent.creditAccountType")
    @Mapping(target = "debitAccountType", source = "taxComponent.debitAccountType")
    @Mapping(target = "glAccountOptions", ignore = true)
    @Mapping(target = "glAccountTypeOptions", ignore = true)
    @Mapping(target = "taxComponentHistories", ignore = true)
    TaxComponentData map(TaxComponent taxComponent);

    List<TaxComponentData> map(List<TaxComponent> taxComponents);

}
