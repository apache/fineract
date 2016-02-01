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
package org.apache.fineract.accounting.rule.exception;

import java.util.Date;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Closure is Invalid
 */
public class AccountingRuleInvalidException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons for invalid Accounting Closure **/
    public static enum GL_CLOSURE_INVALID_REASON {
        FUTURE_DATE, ACCOUNTING_CLOSED;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "Accounting closures cannot be made for a future date";
            } else if (name().toString().equalsIgnoreCase("ACCOUNTING_CLOSED")) { return "Accounting Closure for this branch has already been defined for a greater date"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "error.msg.glclosure.invalid.future.date";
            } else if (name().toString().equalsIgnoreCase("ACCOUNTING_CLOSED")) { return "error.msg.glclosure.invalid.accounting.closed"; }
            return name().toString();
        }
    }

    public AccountingRuleInvalidException(final GL_CLOSURE_INVALID_REASON reason, final Date date) {
        super(reason.errorCode(), reason.errorMessage(), date);
    }
}