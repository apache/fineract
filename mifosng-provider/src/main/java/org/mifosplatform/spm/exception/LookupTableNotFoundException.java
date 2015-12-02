/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.spm.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class LookupTableNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LookupTableNotFoundException(final Long id) {
        super("error.msg.survey.lookuptable.id.notfound", "Lookup table with id " + id + " not found!", id);
    }
}
