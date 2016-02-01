/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.domain;

import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GLAccountRepository} .
 * </p>
 */
@Service
public class GLAccountRepositoryWrapper {

    private final GLAccountRepository repository;

    @Autowired
    public GLAccountRepositoryWrapper(final GLAccountRepository repository) {
        this.repository = repository;
    }

    public GLAccount findOneWithNotFoundDetection(final Long id) {
        final GLAccount account = this.repository.findOne(id);
        if (account == null) { throw new GLAccountNotFoundException(id); }
        return account;
    }

}