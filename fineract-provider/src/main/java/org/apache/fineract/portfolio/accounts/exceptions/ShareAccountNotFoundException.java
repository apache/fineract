/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.exceptions;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class ShareAccountNotFoundException extends AbstractPlatformResourceNotFoundException{

    public ShareAccountNotFoundException(final Long id) {
        super("error.msg.shareaccount.id.invalid", " Account with identifier " + id + " does not exist" , id);
    }
}
