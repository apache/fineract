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
package org.apache.fineract.portfolio.client.domain;

import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientChargeRepositoryWrapper {

    private final ClientChargeRepository repository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;

    @Autowired
    public ClientChargeRepositoryWrapper(final ClientChargeRepository repository,
            final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepositoryWrapper) {
        this.repository = repository;
        this.organisationCurrencyRepository = organisationCurrencyRepositoryWrapper;
    }

    public ClientCharge findOneWithNotFoundDetection(final Long id) {
        final ClientCharge clientCharge = this.repository.findById(id).orElseThrow(() -> new ChargeNotFoundException(id));
        // enrich Client charge with details of Organizational currency
        clientCharge.setCurrency(organisationCurrencyRepository.findOneWithNotFoundDetection(clientCharge.getCharge().getCurrencyCode()));
        return clientCharge;
    }

    public void save(final ClientCharge clientCharge) {
        this.repository.save(clientCharge);
    }

    public void saveAndFlush(final ClientCharge clientCharge) {
        this.repository.saveAndFlush(clientCharge);
    }

    public void delete(final ClientCharge clientCharge) {
        this.repository.delete(clientCharge);
        this.repository.flush();
    }

}
