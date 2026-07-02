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
import org.apache.fineract.investor.validation.ExternalAssetOwnerTransferRequestConstraint;

@ExternalAssetOwnerTransferRequestConstraint
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAssetOwnerSaleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long loanId;

    @NotBlank(message = "{org.apache.fineract.investor.transfer.settlement-date.not-null}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String settlementDate;

    @Size(max = 100, message = "{org.apache.fineract.investor.transfer.owner-external-id.size}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = Integer.MAX_VALUE)
    private String ownerExternalId;

    @Size(max = 50, message = "{org.apache.fineract.investor.transfer.purchase-price-ratio.size}")
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, maxLength = Integer.MAX_VALUE)
    private String purchasePriceRatio;

    @Size(max = 100, message = "{org.apache.fineract.investor.transfer.transfer-external-id.size}")
    @Schema(maxLength = Integer.MAX_VALUE)
    private String transferExternalId;

    @Size(max = 100, message = "{org.apache.fineract.investor.transfer.transfer-external-group-id.size}")
    @Schema(maxLength = Integer.MAX_VALUE)
    private String transferExternalGroupId;

    @Schema(example = "yyyy-MM-dd")
    private String dateFormat;

    @Schema(example = "en")
    private String locale;

    @Hidden
    private String command;
}
