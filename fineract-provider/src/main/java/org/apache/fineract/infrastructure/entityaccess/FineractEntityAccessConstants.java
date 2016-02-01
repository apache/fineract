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
package org.apache.fineract.infrastructure.entityaccess;

public class FineractEntityAccessConstants {

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
