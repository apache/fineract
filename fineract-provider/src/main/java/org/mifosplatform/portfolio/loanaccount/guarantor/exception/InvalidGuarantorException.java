/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.guarantor.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when a Client is set as a
 * guarantor for his/her own loans
 */
public class InvalidGuarantorException extends AbstractPlatformDomainRuleException {

    public InvalidGuarantorException(final Long clientId, final Long loanId) {
        super("error.msg.invalid.guarantor", "Tried to set Client with id " + clientId
                + " as a guarantor to his/her own loan with loan identifier =" + loanId, clientId, loanId);
    }
    
    public InvalidGuarantorException(final Long clientId, final Long loanId,final String errorcode) {
        super("error.msg."+errorcode, "Tried to set Client with id " + clientId
                + " as a guarantor to his/her own loan with loan identifier =" + loanId, clientId, loanId);
    }

}
