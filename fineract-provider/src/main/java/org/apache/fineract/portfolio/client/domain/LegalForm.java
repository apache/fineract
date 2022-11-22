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
package org.apache.fineract.portfolio.client.domain;

import lombok.Getter;

/**
 * Type used to differentiate the type of client
 */
@Getter
public enum LegalForm {

    PERSON(1, "legalFormType.person", "Person"),

    ENTITY(2, "legalFormType.entity", "Entity");

    private final Integer value;
    private final String code;
    private final String label;

    LegalForm(final Integer value, final String code, final String label) {
        this.value = value;
        this.code = code;
        this.label = label;
    }

    public static LegalForm fromInt(final Integer type) {

        LegalForm legalForm = null;
        switch (type) {
            case 1:
                legalForm = LegalForm.PERSON;
            break;
            case 2:
                legalForm = LegalForm.ENTITY;
            break;
        }
        return legalForm;
    }

    public boolean isPerson() {
        return this.value.equals(LegalForm.PERSON.getValue());
    }

    public boolean isEntity() {
        return this.value.equals(LegalForm.ENTITY.getValue());
    }

    @Override
    public String toString() {
        return this.label;
    }
}
