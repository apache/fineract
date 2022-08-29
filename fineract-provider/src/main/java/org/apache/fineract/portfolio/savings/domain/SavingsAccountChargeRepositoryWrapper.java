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
package org.apache.fineract.portfolio.savings.domain;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link SavingsAccountChargeRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class SavingsAccountChargeRepositoryWrapper {

    private final SavingsAccountChargeRepository repository;

    @Autowired
    public SavingsAccountChargeRepositoryWrapper(final SavingsAccountChargeRepository repository) {
        this.repository = repository;
    }

    public SavingsAccountCharge findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new SavingsAccountChargeNotFoundException(id));
    }

    public SavingsAccountCharge findOneWithNotFoundDetection(final Long id, final Long savingsAccountId) {
        final SavingsAccountCharge savingsAccountCharge = this.repository.findByIdAndSavingsAccountId(id, savingsAccountId);
        if (savingsAccountCharge == null) {
            throw new SavingsAccountChargeNotFoundException(id);
        }
        return savingsAccountCharge;
    }

    public List<SavingsAccountCharge> findPendingCharges(final LocalDate transactionDate) {
        return this.repository.findPendingCharges(transactionDate);
    }

    public void save(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.save(savingsAccountCharge);
    }

    public void save(final Iterable<SavingsAccountCharge> savingsAccountCharges) {
        this.repository.saveAll(savingsAccountCharges);
    }

    public void saveAndFlush(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.saveAndFlush(savingsAccountCharge);
    }

    public void delete(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.delete(savingsAccountCharge);
    }
}
