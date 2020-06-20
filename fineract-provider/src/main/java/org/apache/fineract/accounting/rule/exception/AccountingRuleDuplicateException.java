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

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when an Accounting rule with a given name already exists
 */
public class AccountingRuleDuplicateException extends AbstractPlatformDomainRuleException {

    public AccountingRuleDuplicateException(final String name) {
        super("error.msg.accounting.rule.duplicate", "An accounting rule with the name " + name + " already exists" + name);
    }

    public AccountingRuleDuplicateException() {
        super("error.msg.accounting.rule.tag.duplicate", "The accounting rule already have the tags which you defined");
    }

}
