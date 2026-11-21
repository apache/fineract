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
package org.apache.fineract.portfolio.charge.service;

import org.apache.fineract.portfolio.charge.data.ChargeCreateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeCreateResponse;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteRequest;
import org.apache.fineract.portfolio.charge.data.ChargeDeleteResponse;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateRequest;
import org.apache.fineract.portfolio.charge.data.ChargeUpdateResponse;

/**
 * Strongly typed, JsonCommand-free write service backing the command-dispatcher path for charge definitions.
 */
public interface ChargeWriteService {

    ChargeCreateResponse create(ChargeCreateRequest request);

    ChargeUpdateResponse update(ChargeUpdateRequest request);

    ChargeDeleteResponse delete(ChargeDeleteRequest request);
}
