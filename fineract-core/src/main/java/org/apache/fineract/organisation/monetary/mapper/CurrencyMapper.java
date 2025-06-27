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
package org.apache.fineract.organisation.monetary.mapper;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.mapstruct.Mapping;

@org.mapstruct.Mapper(config = MapstructMapperConfig.class)
public interface CurrencyMapper {

    @Mapping(target = "nameCode", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "displaySymbol", ignore = true)
    @Mapping(target = "displayLabel", ignore = true)
    @Mapping(source = "code", target = "code")
    @Mapping(source = "digitsAfterDecimal", target = "decimalPlaces")
    @Mapping(source = "inMultiplesOf", target = "inMultiplesOf")
    CurrencyData map(MonetaryCurrency source);
}
