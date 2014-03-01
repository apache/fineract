/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.Date;
import java.util.List;

import org.mifosplatform.portfolio.charge.exception.SavingsAccountChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link SavingsAccountChargeRepository} that adds NULL checking
 * and Error handling capabilities
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
        final SavingsAccountCharge savingsAccountCharge = this.repository.findOne(id);
        if (savingsAccountCharge == null) { throw new SavingsAccountChargeNotFoundException(id); }
        return savingsAccountCharge;
    }

    public SavingsAccountCharge findOneWithNotFoundDetection(final Long id, final Long savingsAccountId) {
        final SavingsAccountCharge savingsAccountCharge = this.repository.findByIdAndSavingsAccountId(id, savingsAccountId);
        if (savingsAccountCharge == null) { throw new SavingsAccountChargeNotFoundException(id); }
        return savingsAccountCharge;
    }

    public List<SavingsAccountCharge> findPendingCharges(final Date transactionDate) {
        return this.repository.findPendingCharges(transactionDate);
    }

    public void save(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.save(savingsAccountCharge);
    }

    public void save(final Iterable<SavingsAccountCharge> savingsAccountCharges) {
        this.repository.save(savingsAccountCharges);
    }

    public void saveAndFlush(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.saveAndFlush(savingsAccountCharge);
    }

    public void delete(final SavingsAccountCharge savingsAccountCharge) {
        this.repository.delete(savingsAccountCharge);
    }
}