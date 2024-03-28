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
package org.apache.fineract.infrastructure.core.api.mvc;

import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import java.util.Set;
import org.springframework.http.converter.json.MappingJacksonValue;

public class JacksonPartialResponseMappingValue<T> extends MappingJacksonValue {

    public JacksonPartialResponseMappingValue(final T value, final Set<String> filters) {
        super(value);
        setFilters(new SimpleFilterProvider().addFilter(JacksonPartialResponseFilter.PARTIAL_RESPONSE,
                filters.isEmpty() ? SimpleBeanPropertyFilter.serializeAll() : SimpleBeanPropertyFilter.filterOutAllExcept(filters)));
    }
}
