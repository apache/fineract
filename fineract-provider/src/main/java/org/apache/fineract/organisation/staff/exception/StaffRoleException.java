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
package org.apache.fineract.organisation.staff.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when staff resources are not found.
 */
public class StaffRoleException extends AbstractPlatformResourceNotFoundException {

    public static enum STAFF_ROLE {
        LOAN_OFFICER, BRANCH_MANAGER,SAVINGS_OFFICER;

        @Override
        public String toString() {
            return name().toString().replaceAll("-", " ").toLowerCase();
        }
    }

    public StaffRoleException(final Long id, final STAFF_ROLE role) {
        super("error.msg.staff.id.invalid.role", "Staff with identifier " + id + " is not a " + role.toString(), id);
    }
}