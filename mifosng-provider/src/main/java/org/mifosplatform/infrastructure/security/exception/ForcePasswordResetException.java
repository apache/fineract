/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.infrastructure.security.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ForcePasswordResetException extends AbstractPlatformDomainRuleException {
	
	public ForcePasswordResetException(){
		super("error.msg.password.reset.days.value.must.be.greater.than.zero" , "For enabling 'Force Password Reset Days' configuration , the value (number of days after which a user is forced to reset his password) must be set to a number greater than 0.");
	}

}
