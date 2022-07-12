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

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a GL Closure Delte command is invalid
 */
public class AccountingRuleInvalidDeleteException extends AbstractPlatformDomainRuleException {

    public AccountingRuleInvalidDeleteException(final Long officeId, final String officeName, final LocalDate latestclosureDate) {
        super("error.msg.glclosure.invalid.delete", "The latest closure for office with Id " + officeId + " and name " + officeName
                + " is on " + latestclosureDate.toString() + ", please delete this closure first", latestclosureDate);
    }
}
