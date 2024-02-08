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
package org.apache.fineract.portfolio.rate.service;

import java.util.Collection;
import java.util.List;
import org.apache.fineract.portfolio.rate.data.RateData;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */
public interface RateReadService {

    Collection<RateData> retrieveAllRates();

    Collection<RateData> retrieveLoanApplicableRates();

    RateData retrieveOne(Long rateId);

    RateData retrieveByName(String name);

    List<RateData> retrieveProductLoanRates(Long loanId);

    List<RateData> retrieveLoanRates(Long loanId);

}
