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
package org.apache.fineract.organisation.staff.service;

import java.util.Collection;

import org.apache.fineract.organisation.staff.data.StaffData;

public interface StaffReadPlatformService {

    StaffData retrieveStaff(Long staffId);

    Collection<StaffData> retrieveAllStaffForDropdown(Long officeId);

    Collection<StaffData> retrieveAllLoanOfficersInOfficeById(final Long officeId);

    /**
     * returns all staff in offices that are above the provided
     * <code>officeId</code>.
     */
    Collection<StaffData> retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(Long officeId, boolean loanOfficersOnly);

    Collection<StaffData> retrieveAllStaff(String sqlSearch, Long officeId, boolean loanOfficersOnly, String status);
    
    Object[] hasAssociatedItems(final Long staffId);
}