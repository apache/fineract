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
package org.apache.fineract.investor.data.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAssetOwnerCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @jakarta.validation.constraints.NotBlank(message = "{validation.msg.externalAssetOwner.ownerExternalId.cannot.be.blank}")
    @Size(max = 100, message = "{org.apache.fineract.investor.transfer.owner-external-id.size}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = Integer.MAX_VALUE)
    private String ownerExternalId;
}
