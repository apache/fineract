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
package org.apache.fineract.portfolio.floatingrates.service;

import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateCreateResponse;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateRequest;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateUpdateResponse;

/**
 * Strongly typed, JsonCommand-free write service backing the new command-dispatcher path for floating rates.
 */
public interface FloatingRateWriteService {

    FloatingRateCreateResponse create(FloatingRateCreateRequest request);

    FloatingRateUpdateResponse update(FloatingRateUpdateRequest request);
}
