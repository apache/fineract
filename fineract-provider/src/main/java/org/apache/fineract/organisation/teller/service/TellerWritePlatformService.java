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
package org.apache.fineract.organisation.teller.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

/**
 * Provides the local service for adding, modifying and deleting tellers.
 *
 * @author Markus Geiss
 * @see org.apache.fineract.organisation.teller.domain.TellerRepository
 * @since 2.0.0
 */
public interface TellerWritePlatformService {

    /**
     * Creates a new teller.
     *
     * @param command the command to create a new teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.apache.fineract.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult createTeller(JsonCommand command);

    /**
     * Modifies a new teller.
     *
     * @param tellerId the primary key of the teller
     * @param command  the command to modifya new teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.apache.fineract.organisation.teller.exception.TellerNotFoundException
     * @throws org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.apache.fineract.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult modifyTeller(Long tellerId, JsonCommand command);

    /**
     * deletes a new teller.
     *
     * @param tellerId the primary key of the teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.apache.fineract.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult deleteTeller(Long tellerId);
    
    /**
     * Allocates a cashier to an existing teller. The allocation can be for a duration
     * from a date to a date
     * from a certain start time to an end time.
     *
	 * @param command the command to allocate a cashier for a specific teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.apache.fineract.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult allocateCashierToTeller(Long tellerId, JsonCommand command);

	CommandProcessingResult updateCashierAllocation(Long tellerId, Long cashierId,
			JsonCommand command);

	CommandProcessingResult deleteCashierAllocation(final Long tellerId, Long cashierId, 
			JsonCommand command);

	CommandProcessingResult allocateCashToCashier(Long cashierId,
			JsonCommand command);

	CommandProcessingResult settleCashFromCashier(Long cashierId,
			JsonCommand command);
}
