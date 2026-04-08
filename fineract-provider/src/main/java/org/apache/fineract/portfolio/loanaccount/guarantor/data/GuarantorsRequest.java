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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class GuarantorsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String locale;
    private String dateFormat; // "dd MMMM yyyy"

    /*** Fields for capturing relationship of Guarantor with customer **/
    private Long clientRelationshipTypeId;

    /*** Fields for current customers serving as guarantors **/
    private Integer guarantorTypeId;
    private Long entityId;

    /*** Fields for external persons serving as guarantors ***/
    private String firstname;
    private String lastname;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String mobileNumber;
    private String housePhoneNumber;
    private String comment;
    private String dob;
    private Long savingsId;
    private BigDecimal amount;

}
