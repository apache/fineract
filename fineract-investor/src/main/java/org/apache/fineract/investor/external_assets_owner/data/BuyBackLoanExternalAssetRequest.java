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
public class BuyBackLoanExternalAssetRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long loanId;

    @Size(max = 100, message = "{org.apache.fineract.investor.external_assets_owner.data.BuyBackLoanExternalAssetRequest.transferExternalId.size}")
    private String transferExternalId;

    @NotNull(message = "{org.apache.fineract.investor.external_assets_owner.data.BuyBackLoanExternalAssetRequest.settlementDate.required}")
    private String settlementDate;

    private String dateFormat;
    private String locale;
}
