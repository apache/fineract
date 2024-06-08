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
package org.apache.fineract.infrastructure.creditbureau.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_creditbureau")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreditBureau extends AbstractPersistableCustom<Long> {

    private String name;

    private String product;

    private String country;

    @Column(name = "implementation_key")
    private String implementationKey;

    public static CreditBureau fromJson(final JsonCommand command) {

        final String tname = command.stringValueOfParameterNamed("name");
        final String tproduct = command.stringValueOfParameterNamed("product");
        final String tcountry = command.stringValueOfParameterNamed("country");
        final String timplementationKey = command.stringValueOfParameterNamed("implementationKey");

        return new CreditBureau().setName(tname).setProduct(tproduct).setCountry(tcountry).setImplementationKey(timplementationKey);
    }

}
