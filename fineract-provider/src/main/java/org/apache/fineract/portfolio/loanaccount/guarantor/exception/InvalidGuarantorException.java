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
package org.apache.fineract.portfolio.loanaccount.guarantor.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when a Client is set as a guarantor for his/her own loans
 */
public class InvalidGuarantorException extends AbstractPlatformDomainRuleException {

    public InvalidGuarantorException(final Long clientId, final Long loanId) {
        super("error.msg.invalid.guarantor",
                "Tried to set Client with id " + clientId + " as a guarantor to his/her own loan with loan identifier =" + loanId, clientId,
                loanId);
    }

    public InvalidGuarantorException(final Long clientId, final Long loanId, final String errorcode) {
        super("error.msg." + errorcode,
                "Tried to set Client with id " + clientId + " as a guarantor to his/her own loan with loan identifier =" + loanId, clientId,
                loanId);
    }

}
