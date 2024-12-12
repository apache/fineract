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
package org.apache.fineract.portfolio.interestpauses.service;

import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

public interface InterestPauseWritePlatformService {

    CommandProcessingResult createInterestPause(ExternalId loanExternalId, String startDate, String endDate, String dateFormat,
            String locale);

    /**
     * Create a new interest pause period for a loan identified by its internal ID.
     *
     * @param loanId
     *            the ID of the loan
     * @param startDate
     *            the start date of the interest pause period (inclusive)
     * @param endDate
     *            the end date of the interest pause period (inclusive)
     * @return the ID of the created loan term variation representing the interest pause
     */
    CommandProcessingResult createInterestPause(Long loanId, String startDate, String endDate, String dateFormat, String locale);
}
