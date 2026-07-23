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
package org.apache.fineract.portfolio.client.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientIdentifierUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Hidden
    private Long id;

    @Hidden
    private Long clientId;

    // Not validated here: the legacy update path never checked positivity either, so a non-positive id just falls
    // through to the code-value lookup, which 404s instead of 400ing.
    private Long documentTypeId;

    private String documentKey;

    private String status;

    private String description;

    // Kept as Strings, parsed with dateFormat/locale in ClientIdentifierWriteServiceImpl (see MeetingCreateRequest for
    // the same pattern). Not sent at all leaves the stored date unchanged; an explicit JSON null or an empty string
    // clears it, same as the legacy path. Jackson calls the setters below for an explicit null too, which is how the
    // two cases are told apart.
    private String issuanceDate;
    private String expiryDate;
    private String locale;
    private String dateFormat;

    @Hidden
    @JsonIgnore
    private boolean issuanceDateProvided;

    @Hidden
    @JsonIgnore
    private boolean expiryDateProvided;

    public void setIssuanceDate(final String issuanceDate) {
        this.issuanceDate = issuanceDate;
        this.issuanceDateProvided = true;
    }

    public void setExpiryDate(final String expiryDate) {
        this.expiryDate = expiryDate;
        this.expiryDateProvided = true;
    }
}
