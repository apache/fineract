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
package org.apache.fineract.portfolio.loanproduct.domain;

import java.util.List;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link LoanProductScorecardFeatureRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class LoanProductScorecardFeatureRepositoryWrapper {

    private final LoanProductScorecardFeatureRepository repository;

    @Autowired
    public LoanProductScorecardFeatureRepositoryWrapper(final LoanProductScorecardFeatureRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public LoanProductScorecardFeature findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new FeatureNotFoundException(id));
    }

    public LoanProductScorecardFeature saveAndFlush(final LoanProductScorecardFeature scorecardFeature) {
        return this.repository.saveAndFlush(scorecardFeature);
    }

    @Transactional
    public LoanProductScorecardFeature save(final LoanProductScorecardFeature scorecardFeature) {
        return this.repository.save(scorecardFeature);
    }

    public List<LoanProductScorecardFeature> save(List<LoanProductScorecardFeature> scorecardFeatures) {
        return this.repository.saveAll(scorecardFeatures);
    }

    public void flush() {
        this.repository.flush();
    }

    public void delete(final Long featureId) {
        this.repository.deleteById(featureId);
    }

}
