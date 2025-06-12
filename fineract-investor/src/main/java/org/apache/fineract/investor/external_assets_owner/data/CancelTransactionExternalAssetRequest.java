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
package org.apache.fineract.investor.external_assets_owner.data;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelTransactionExternalAssetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long transferId;

    @NotBlank(message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.ownerExternalId.required}")
    @Size(max = 100, message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.ownerExternalId.size}")
    private String ownerExternalId;

    @Size(max = 100, message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.transferExternalId.size}")
    private String transferExternalId;

    @NotBlank(message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.purchasePriceRatio.required}")
    @Size(max = 50, message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.purchasePriceRatio.size}")
    private String purchasePriceRatio;

    @NotNull(message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.settlementDate.required}")
    private String settlementDate;

    @Size(max = 100, message = "{org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest.transferExternalGroupId.size}")
    private String transferExternalGroupId;

    private String dateFormat;
    private String locale;
}
