/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

/**
 * Type used to differentiate the type of client
 */
public enum LegalForm {
	
	PERSON(1, "legalFormType.person"),
	
	ENTITY(2, "legalFormType.entity");
	
	private final Integer value;
    private final String code;
	
    private LegalForm(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }
    
    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
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
}
