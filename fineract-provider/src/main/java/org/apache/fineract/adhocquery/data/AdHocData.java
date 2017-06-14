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

import java.util.Collection;

import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.joda.time.DateTime;
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
	private final String email;
	@SuppressWarnings("unused")
    private final boolean isActive;
	@SuppressWarnings("unused")
    private final DateTime createdOn;
	@SuppressWarnings("unused")
	private final Long createdById;
	@SuppressWarnings("unused")
    private final Long updatedById;   
	@SuppressWarnings("unused")
    private final DateTime updatedOn;
	@SuppressWarnings("unused")
	private final String createdBy;
	
    
	

    public AdHocData(final Long id, final String name,final String query, final String tableName,final String tableFields, 
    		final boolean isActive, final DateTime createdOn, final Long createdById,final Long updatedById,
    		final DateTime updatedOn,final String createdBy,final String email
            ) {
        this.id = id;
        this.name=name;
        this.query=query;
        this.tableName = tableName;
        this.tableFields = tableFields;
        this.isActive = isActive;
        this.createdOn = createdOn;
        this.createdById = createdById;
        this.updatedById=updatedById;
        this.updatedOn=updatedOn;
        this.createdBy=createdBy;
        this.email=email;
    }
    public static AdHocData template() {
        AdHocData adHocData = new AdHocData(null,null,null,null,null,false,null,null,null,null,null,null);
		return adHocData;
    }
    public Long getId() {
		return this.id;
	}
	public String getName() {
		return this.name;
	}
	public String getQuery() {
		return this.query;
	}
	public String getTableName() {
		return this.tableName;
	}
	public String getTableFields() {
		return this.tableFields;
	}
	public String getEmail() {
		return this.email;
	}
	public boolean isActive() {
		return this.isActive;
	}
	public DateTime getCreatedOn() {
		return this.createdOn;
	}
	public Long getCreatedById() {
		return this.createdById;
	}
	public Long getUpdatedById() {
		return this.updatedById;
	}
	public DateTime getUpdatedOn() {
		return this.updatedOn;
	}
	public String getCreatedBy() {
		return this.createdBy;
	}
}