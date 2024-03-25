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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.apache.fineract.infrastructure.core.api.mvc.validation.MaxSize;
import org.apache.fineract.infrastructure.core.api.mvc.validation.ResourceName;

@ResourceName("fund")
@FieldNameConstants
@Getter
@Setter
public class PutFundsRequest {

    // TODO: Using validation on a generic type like Optional will lead to duplicate validation errors.
    @Schema(example = "EU Agri Fund")
    private Optional<@NotBlank(message = "is mandatory") @MaxSize(max = 100) String> name;

    @Schema(example = "123")
    private Optional<@Size(max = 100) String> externalId;
}
