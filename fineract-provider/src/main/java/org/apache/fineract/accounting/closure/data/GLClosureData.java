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
package org.apache.fineract.accounting.closure.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import lombok.Data;
import org.apache.fineract.organisation.office.data.OfficeData;

/**
 * Immutable object representing a General Ledger Account
 *
 * Note: no getter/setters required as google-gson will produce json from fields of object.
 */
@Data
public class GLClosureData {

    private final Long id;
    private final Long officeId;
    private final String officeName;
    private final LocalDate closingDate;
    private final boolean deleted;
    private final LocalDate createdDate;
    private final LocalDate lastUpdatedDate;
    private final Long createdByUserId;
    private final String createdByUsername;
    private final Long lastUpdatedByUserId;
    private final String lastUpdatedByUsername;
    private final String comments;

    private Collection<OfficeData> allowedOffices = new ArrayList<>();
}
