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
package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class UnlockSavingsAccountException extends AbstractPlatformDomainRuleException {

    public enum UnlockSavingsAccountExceptionType {

        FUTURE_DATE, VALID_DATE, ACTIVATION_DATE, INVALID_ACCOUNT_TYPE, INVALID_ACCOUNT_STATUS, INVALID_LOCK_IN_DETAILS, UNLOCK_DATE_IS_AFTER_LOCKED_DATE, ACCOUNT_ALREADY_UNLOCKED;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "Cannot Post Interest in future Dates";
            } else if (name().toString().equalsIgnoreCase("VALID_DATE")) {
                return "Please Pass a valid date";
            } else if (name().toString().equalsIgnoreCase("ACTIVATION_DATE")) {
                return "Unlock Date must be after the Activation date";
            } else if (name().toString().equalsIgnoreCase("INVALID_ACCOUNT_TYPE")) {
                return "Invalid AccountType Detected . Expected GSIM Account Type";
            } else if (name().toString().equalsIgnoreCase("INVALID_ACCOUNT_STATUS")) {
                return "Invalid Account Status Detected . Expected ACTIVE Account STATUS";
            } else if (name().toString().equalsIgnoreCase("INVALID_LOCK_IN_DETAILS")) {
                return "Invalid LockIn Details Detected ";
            } else if (name().toString().equalsIgnoreCase("UNLOCK_DATE_IS_AFTER_LOCKED_DATE")) {
                return "Unlock Date can't be after locked period ";
            } else if (name().toString().equalsIgnoreCase("ACCOUNT_ALREADY_UNLOCKED")) {
                return "This Account is Already Unlocked ";
            }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("FUTURE_DATE")) {
                return "error.msg.futureDate";
            } else if (name().toString().equalsIgnoreCase("VALID_DATE")) {
                return "error.msg.nullDatePassed";
            } else if (name().toString().equalsIgnoreCase("ACTIVATION_DATE")) {
                return "error.msg.before activation date";
            } else if (name().toString().equalsIgnoreCase("INVALID_ACCOUNT_TYPE")) {
                return "error.msg.invalid.account.type";
            } else if (name().toString().equalsIgnoreCase("INVALID_ACCOUNT_STATUS")) {
                return "error.msg.invalid.account.status";
            } else if (name().toString().equalsIgnoreCase("INVALID_LOCK_IN_DETAILS")) {
                return "error.msg.invalid.lockIn.details";
            } else if (name().toString().equalsIgnoreCase("UNLOCK_DATE_IS_AFTER_LOCKED_DATE")) {
                return "error.msg.unlock.date.shouldn't.be.after.locked.date";
            } else if (name().toString().equalsIgnoreCase("ACCOUNT_ALREADY_UNLOCKED")) {
                return "error.msg.account.already.unlocked";
            }
            return name().toString();
        }
    }

    public UnlockSavingsAccountException(final UnlockSavingsAccountExceptionType reason) {
        super(reason.errorCode(), reason.errorMessage());
    }

}
