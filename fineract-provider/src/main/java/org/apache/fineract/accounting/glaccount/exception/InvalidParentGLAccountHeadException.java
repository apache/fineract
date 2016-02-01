/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class InvalidParentGLAccountHeadException extends AbstractPlatformDomainRuleException {

    public InvalidParentGLAccountHeadException(final Long glAccountId, final Long parentId) {
        super("error.msg.glaccount.id.and.parentid.must.not.same", "parentId:" + parentId + ", id" + glAccountId + " should not be same",
                glAccountId, parentId);
    }

}
