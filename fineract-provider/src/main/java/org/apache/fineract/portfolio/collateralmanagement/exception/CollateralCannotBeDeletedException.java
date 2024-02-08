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
package org.apache.fineract.portfolio.collateralmanagement.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CollateralCannotBeDeletedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Collateral cannot be waived **/
    public enum CollateralCannotBeDeletedReason {

        COLLATERAL_IS_ALREADY_ATTACHED;

        public String errorMessage() {
            if (name().equalsIgnoreCase("COLLATERAL_IS_ALREADY_ATTACHED")) {
                return "This collateral cannot be deleted as this is associated with one or more client collaterals";
            }
            return name();
        }

        public String errorCode() {
            if (name().equalsIgnoreCase("COLLATERAL_IS_ALREADY_ATTACHED")) {
                return "error.msg.collateral.is.already.associated.with.client.collateral";
            }
            return name();
        }
    }

    public CollateralCannotBeDeletedException(final CollateralCannotBeDeletedReason reason, final Long loanCollateralId) {
        super(reason.errorCode(), reason.errorMessage(), loanCollateralId);
    }

}
