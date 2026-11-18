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
package org.apache.fineract.portfolio.collateralmanagement.service;

import java.util.Set;
import org.apache.fineract.portfolio.client.data.ClientCollateralManagementData;

/**
 * Core read-contract returning a client's collateral as DTOs, so that the client feature does not need a compile-time
 * dependency on the collateral-management domain/repository types. Implemented by the collateral-management module.
 */
public interface ClientCollateralReadService {

    Set<ClientCollateralManagementData> retrieveCollateralDataForClient(Long clientId);
}
