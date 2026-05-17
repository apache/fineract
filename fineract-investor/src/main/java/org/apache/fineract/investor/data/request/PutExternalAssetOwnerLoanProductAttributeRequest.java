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

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(name = "PutExternalAssetOwnerLoanProductAttributeRequest")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutExternalAssetOwnerLoanProductAttributeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long loanProductId;

    @Hidden
    private Long attributeId;

    @NotBlank(message = "{validation.msg.externalAssetOwnerLoanProductAttribute.attributeKey.cannot.be.blank}")
    @Size(max = 255, message = "{validation.msg.externalAssetOwnerLoanProductAttribute.attributeKey.max.length}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = Integer.MAX_VALUE)
    private String attributeKey;

    @NotBlank(message = "{validation.msg.externalAssetOwnerLoanProductAttribute.attributeValue.cannot.be.blank}")
    @Size(max = 255, message = "{validation.msg.externalAssetOwnerLoanProductAttribute.attributeValue.max.length}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = Integer.MAX_VALUE)
    private String attributeValue;
}
