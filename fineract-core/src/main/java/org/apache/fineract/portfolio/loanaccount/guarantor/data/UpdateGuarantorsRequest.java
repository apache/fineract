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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.fineract.validation.constraints.Locale;
import org.apache.fineract.validation.constraints.ValidAge;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidAge(dateField = "dob", formatField = "dateFormat", localeField = "locale", min = 15, max = 75, message = "{guarantor.dob.validAge}")
public class UpdateGuarantorsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @PositiveOrZero(message = "{guarantor.loanId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{guarantor.loanId.digits}")
    private Long loanId;

    @PositiveOrZero(message = "{guarantor.guarantorId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{guarantor.guarantorId.digits}")
    private Long guarantorId;

    @Size(max = 50, message = "{guarantor.locale.size}")
    @Locale
    private String locale;

    @Pattern(regexp = "dd MMMM yyyy", message = "{guarantor.dateFormat.invalid}")
    private String dateFormat;

    /*** Fields for capturing relationship of Guarantor with customer **/
    @PositiveOrZero(message = "{guarantor.clientRelationshipTypeId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{guarantor.clientRelationshipTypeId.digits}")
    private Long clientRelationshipTypeId;

    /*** Fields for current customers serving as guarantors **/
    @Min(value = 1, message = "{guarantor.guarantorTypeId.min}")
    @Max(value = 3, message = "{guarantor.guarantorTypeId.max}")
    @Digits(integer = 10, fraction = 0, message = "{guarantor.guarantorTypeId.digits}")
    private Integer guarantorTypeId;

    @PositiveOrZero(message = "{guarantor.entityId.positiveOrZero}")
    @Digits(integer = 10, fraction = 0, message = "{guarantor.entityId.digits}")
    private Long entityId;

    /*** Fields for external persons serving as guarantors ***/
    @Size(max = 50, message = "{guarantor.firstname.size}")
    private String firstname;

    @Size(max = 50, message = "{guarantor.lastname.size}")
    private String lastname;

    @Size(max = 500, message = "{guarantor.addressLine1.size}")
    private String addressLine1;

    @Size(max = 500, message = "{guarantor.addressLine2.size}")
    private String addressLine2;

    @Size(max = 50, message = "{guarantor.city.size}")
    private String city;

    @Size(max = 50, message = "{guarantor.state.size}")
    private String state;

    @Size(max = 20, message = "{guarantor.zip.size}")
    private String zip;

    @Size(max = 20, message = "{guarantor.country.size}")
    private String country;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "{guarantor.mobileNumber.invalid}")
    @Size(max = 20, message = "{guarantor.mobileNumber.size}")
    private String mobileNumber;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "{guarantor.housePhoneNumber.invalid}")
    @Size(max = 20, message = "{guarantor.housePhoneNumber.size}")
    private String housePhoneNumber;

    @Size(max = 500, message = "{guarantor.comment.size}")
    private String comment;

    @Size(max = 50, message = "{guarantor.dob.size}")
    private String dob;

    @PositiveOrZero(message = "{guarantor.savingsId.positiveOrZero}")
    private Long savingsId;

    @DecimalMin(value = "0.01", message = "{guarantor.amount.min}")
    private BigDecimal amount;
}
