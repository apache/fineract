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
package org.apache.fineract.infrastructure.dataqueries.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link AbstractPlatformDomainRuleException} thrown when datatable resources are not found.
 */
public class DatatableEntryRequiredException extends AbstractPlatformDomainRuleException {

    public DatatableEntryRequiredException(String datatableName) {
        super("error.msg.entry.required.in.datatable." + datatableName,
                "The datatable " + datatableName + " needs to be filled in before the current action can be proceeded", datatableName);
    }

    public DatatableEntryRequiredException(String datatableName, Long appTableId) {
        super("error.msg.entry.cannot.be.deleted.datatable." + datatableName + ".attached.to.entity.datatable.check",
                "The entry cannot be deleted, due to datatable " + datatableName + " is attached to an Entity-Datatable check",
                datatableName, appTableId);
    }
}
