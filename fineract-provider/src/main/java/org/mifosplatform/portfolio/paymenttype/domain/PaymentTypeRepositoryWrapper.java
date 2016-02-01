/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.domain;

import org.mifosplatform.portfolio.paymenttype.exception.PaymentTypeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentTypeRepositoryWrapper {
    
    private final PaymentTypeRepository repository;

    @Autowired
    public PaymentTypeRepositoryWrapper(final PaymentTypeRepository repository) {
        this.repository = repository;
    }

    public PaymentType findOneWithNotFoundDetection(final Long id) {
        final PaymentType paymentType = this.repository.findOne(id);
        if (paymentType == null) { throw new PaymentTypeNotFoundException(id); }
        return paymentType;
    }

}
