/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class MifosEntityToEntityMappingDateException extends AbstractPlatformResourceNotFoundException {

    public MifosEntityToEntityMappingDateException(final String startDate,final String endDate) {
        super("error.msg.invalid.endDate", "EndDate " + endDate + " cannot be before StartDate "+startDate);
    }
}
