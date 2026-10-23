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
package org.apache.fineract.infrastructure.configuration.data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Immutable data object for global configuration property.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GlobalConfigurationPropertyData {

    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private boolean enabled;
    @SuppressWarnings("unused")
    private Long value;
    @SuppressWarnings("unused")
    private LocalDate dateValue;
    private String stringValue;
    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private boolean trapDoor;
    @Schema(description = "The values stringValue is restricted to, in their canonical spelling. Matching ignores case and surrounding"
            + " whitespace, and an update stores the canonical spelling. Omitted if the configuration is not restricted.", example = "[\"eod\", \"real-time\"]")
    private List<String> allowedValues;
    @Schema(description = "Whether stringValue holds a comma-separated list of allowedValues instead of a single one. Omitted if the"
            + " configuration is not restricted.", example = "false")
    private Boolean multipleValuesAllowed;
}
