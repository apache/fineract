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
package org.apache.fineract.portfolio.loanaccount.guarantor.data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GuarantorsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String locale;
    private final String dateFormat; // "dd MMMM yyyy"

    /*** Fields for capturing relationship of Guarantor with customer **/
    private final Long clientRelationshipTypeId;

    /*** Fields for current customers serving as guarantors **/
    private final Integer guarantorTypeId;
    private final Long entityId;

    /*** Fields for external persons serving as guarantors ***/
    private final String firstname;
    private final String lastname;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;
    private final String mobileNumber;
    private final String housePhoneNumber;
    private final String comment;
    private final LocalDate dob;
    private final Long savingsId;
    private final BigDecimal amount;

}
