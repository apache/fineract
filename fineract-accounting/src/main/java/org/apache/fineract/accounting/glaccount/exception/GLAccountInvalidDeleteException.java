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
package org.apache.fineract.accounting.glaccount.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Account with a given GL Code of the particular type is already present
 */
public class GLAccountInvalidDeleteException extends AbstractPlatformDomainRuleException {

    /*** Enum of reasons for invalid delete **/
    public enum GlAccountInvalidDeleteReason {

        TRANSACTIONS_LOGGED, //
        HAS_CHILDREN, //
        PRODUCT_MAPPING; //

        public String errorMessage() {
            if (name().equalsIgnoreCase("TRANSACTIONS_LOGGED")) {
                return "This GL Account cannot be deleted as it has transactions logged against it";
            } else if (name().equalsIgnoreCase("HAS_CHILDREN")) {
                return "Cannot delete this Header GL Account without first deleting or reassigning its children";
            } else if (name().equalsIgnoreCase("PRODUCT_MAPPING")) {
                return "Cannot delete this GL Account as it is mapped to a Product";
            }
            return name();
        }

        public String errorCode() {
            if (name().equalsIgnoreCase("TRANSACTIONS_LOGGED")) {
                return "error.msg.glaccount.glcode.invalid.delete.transactions.logged";
            } else if (name().equalsIgnoreCase("HAS_CHILDREN")) {
                return "error.msg.glaccount.glcode.invalid.delete.has.children";
            } else if (name().equalsIgnoreCase("PRODUCT_MAPPING")) {
                return "error.msg.glaccount.glcode.invalid.delete.product.mapping";
            }
            return name();
        }
    }

    public GLAccountInvalidDeleteException(final GlAccountInvalidDeleteReason reason, final Long glAccountId) {
        super(reason.errorCode(), reason.errorMessage(), glAccountId);
    }
}
