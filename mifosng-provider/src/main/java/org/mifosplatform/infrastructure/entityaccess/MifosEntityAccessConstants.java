/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess;

public class MifosEntityAccessConstants {

	public static final String GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS = "office-specific-products-enabled";
	public static final String GLOBAL_CONFIG_FOR_RESTRICT_PRODUCTS_TO_USER_OFFICE = "restrict-products-to-user-office";
    public static final String ENTITY_ACCESS_CODENAME = "Entity to Entity Access Types";

    /***
     * Enum of all parameters passed in while creating/updating an entity access
     ***/
    public static enum ENTITY_ACCESS_JSON_INPUT_PARAMS {
        ENTITY_TYPE("entityType"),
    	ENTITY_ID("entityId"),
        ENTITY_ACCESS_TYPE_ID("entityAccessTypeId"),
        SECOND_ENTITY_TYPE("secondEntityType"),
    	SECOND_ENTITY_ID("secondEntityId")
        ;

        private final String value;

        private ENTITY_ACCESS_JSON_INPUT_PARAMS(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
