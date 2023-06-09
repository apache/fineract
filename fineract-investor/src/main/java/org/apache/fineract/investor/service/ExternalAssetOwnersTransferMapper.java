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
package org.apache.fineract.investor.service;

import org.apache.fineract.accounting.journalentry.JournalEntryMapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferDataDetails;
import org.apache.fineract.investor.data.ExternalTransferOwnerData;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class, uses = JournalEntryMapper.class)
public interface ExternalAssetOwnersTransferMapper {

    @Mapping(target = "transferId", source = "id")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "loan.loanId", source = "loanId")
    @Mapping(target = "loan.externalId", source = "externalLoanId")
    @Mapping(target = "transferExternalId", source = "externalId")
    @Mapping(target = "effectiveFrom", source = "effectiveDateFrom")
    @Mapping(target = "effectiveTo", source = "effectiveDateTo")
    @Mapping(target = "purchasePriceRatio", source = "purchasePriceRatio")
    @Mapping(target = "settlementDate", source = "settlementDate")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "details", source = "externalAssetOwnerTransferDetails")
    ExternalTransferData mapTransfer(ExternalAssetOwnerTransfer source);

    ExternalTransferOwnerData mapOwner(ExternalAssetOwner source);

    @Mapping(target = "detailsId", source = "id")
    ExternalTransferDataDetails mapDetails(ExternalAssetOwnerTransferDetails details);

}
