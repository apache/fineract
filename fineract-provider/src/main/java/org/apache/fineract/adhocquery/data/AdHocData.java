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
package org.apache.fineract.adhocquery.data;

import org.joda.time.LocalDate;

/**
 * Immutable data object represent note or case information AdHocData
 * 
 */
public class AdHocData {

	
    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
	@SuppressWarnings("unused")
    private final String query;
	@SuppressWarnings("unused")
    private final String tableName;
	@SuppressWarnings("unused")
    private final String tableFields;
	@SuppressWarnings("unused")
    private final boolean isActive;
	@SuppressWarnings("unused")
	private final LocalDate createdDate;
	@SuppressWarnings("unused")
	private final Long createdByUserId;
	   
    

    public AdHocData(final Long id, final String name,final String query, final String tableName,final String tableFields, 
    		final boolean isActive,final LocalDate createdDate, final Long createdByUserId
            ) {
        this.id = id;
        this.name=name;
        this.query=query;
        this.tableName = tableName;
        this.tableFields = tableFields;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.createdByUserId = createdByUserId;
    }
}