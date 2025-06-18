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

public class ExternalAssetOwnerNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ExternalAssetOwnerNotFoundException(ExternalId externalId) {
        super("error.msg.external.asset.owner.external.id",
                String.format("External asset owner with external id: %s does not found", externalId.getValue()), externalId.getValue());
    }

    public ExternalAssetOwnerNotFoundException(Long id) {
        super("error.msg.external.asset.owner.id", String.format("External asset owner with id: %s does not found", id), id);
    }
}
