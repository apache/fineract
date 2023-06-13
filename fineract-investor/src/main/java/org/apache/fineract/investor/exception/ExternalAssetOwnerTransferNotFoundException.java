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
package org.apache.fineract.investor.exception;

import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.investor.data.ExternalTransferStatus;

public class ExternalAssetOwnerTransferNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ExternalAssetOwnerTransferNotFoundException(ExternalId externalId, ExternalTransferStatus externalTransferStatus) {
        super("error.msg.external.asset.owner.transfer.external.id.and.status",
                String.format("External asset owner transfer with external id: %s and status: %s does not found", externalId.getValue(),
                        externalTransferStatus),
                externalId.getValue(), externalTransferStatus);
    }

    public ExternalAssetOwnerTransferNotFoundException(Long id) {
        super("error.msg.external.asset.owner.transfer.id", String.format("External asset owner transfer with id: %s does not found", id),
                id);
    }
}
