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

package org.apache.fineract.portfolio.accountdetails.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@SuppressWarnings("unused")
@Entity
public class GsimMemberSearch {

    @Id
    private Long id;
    private String name;
    private String officeName;
    private String accountNumber;
    private Long savingsAccountId;
    private Long groupId;

    public GsimMemberSearch() {}

    public GsimMemberSearch(Long id, String name, String officeName, String accountNumber, Long savingsAccountId, Long groupId) {
        this.id = id;
        this.name = name;
        this.officeName = officeName;
        this.accountNumber = accountNumber;
        this.savingsAccountId = savingsAccountId;
        this.groupId = groupId;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOfficeName() {
        return officeName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public Long getGroupId() {
        return groupId;
    }
}
