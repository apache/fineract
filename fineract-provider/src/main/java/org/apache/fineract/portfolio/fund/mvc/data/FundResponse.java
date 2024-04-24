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
package org.apache.fineract.portfolio.fund.mvc.data;

import com.fasterxml.jackson.annotation.JsonFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.fineract.infrastructure.core.api.mvc.JacksonPartialResponseFilter;

/**
 * Immutable data object to represent fund data.
 */
@JsonFilter(JacksonPartialResponseFilter.PARTIAL_RESPONSE)
public record FundResponse(@Schema(example = "1") Long id, @Schema(example = "EU Agri Fund") String name,
        @Schema(example = "123") String externalId) {
}
