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
package org.apache.fineract.migration.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.fineract.migration.domain.LoanMigrationStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request DTO for initiating a loan migration")
public class LoanMigrationRequestDto {

    @Schema(example = "MIGRATION_IN_PROGRESS", allowableValues = { "MIGRATION_IN_PROGRESS", "MIGRATION_SUCCESSFUL", "MIGRATION_FAILED" })
    private LoanMigrationStatus action;

    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializing request to JSON", e);
        }
    }
}
