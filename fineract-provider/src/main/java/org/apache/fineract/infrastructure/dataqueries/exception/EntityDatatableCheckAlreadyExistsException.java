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
package org.apache.fineract.infrastructure.dataqueries.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when datatable resources are not found.
 */
public class EntityDatatableCheckAlreadyExistsException extends AbstractPlatformDomainRuleException {

	public EntityDatatableCheckAlreadyExistsException(final String entityName, final Long status,
			final String datatableName) {
		super("error.msg.entityDatatableCheck.duplicate.entry",
				"the entity datatable check for status: '" + status + "' and datatable name '" + datatableName
				+ "' on entity '" + entityName + "' already exist",
				"status", "datatableName", "entity", status, datatableName, entityName);
	}

	public EntityDatatableCheckAlreadyExistsException(final String entityName, long status, String datatableName,
			long productId) {
		super("error.msg.entityDatatableCheck.duplicate.entry",
				"the entity datatable check for status: '" + status + "' and datatable name '" + datatableName
                + "' on entity '" + entityName + "' and product id '" + productId + "' already exist",
				"status", "datatableName", "entity", "productId", status, datatableName, entityName, productId);
	}
}