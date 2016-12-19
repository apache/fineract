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
package org.apache.fineract.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EntityTables {

	CLIENT("m_client",
        new Integer[]{StatusEnum.CREATE.getCode(),
                StatusEnum.ACTIVATE.getCode(),
                StatusEnum.CLOSE.getCode()},
        "client_id"),
    LOAN("m_loan",
        new Integer[]{StatusEnum.CREATE.getCode(),
                StatusEnum.APPROVE.getCode(),
                StatusEnum.DISBURSE.getCode(),
                StatusEnum.WITHDRAWN.getCode(),
                StatusEnum.REJECTED.getCode(),
                StatusEnum.WRITE_OFF.getCode()},
        "loan_id"),
    GROUP("m_group",
        new Integer[]{StatusEnum.CREATE.getCode(),
                StatusEnum.ACTIVATE.getCode(),
                StatusEnum.CLOSE.getCode(),},
        "group_id"),
    SAVING("m_savings_account",
        new Integer[]{StatusEnum.CREATE.getCode(),
                StatusEnum.APPROVE.getCode(),
                StatusEnum.ACTIVATE.getCode(),
                StatusEnum.WITHDRAWN.getCode(),
                StatusEnum.REJECTED.getCode(),
                StatusEnum.CLOSE.getCode()},
        "savings_account_id");

	private static final Map<String, EntityTables> lookup = new HashMap<String, EntityTables>();
	static {
		for (EntityTables d : EntityTables.values())
			lookup.put(d.getName(), d);
	}

	private String name;

	private Integer[] codes;

	private String foreignKeyColumnNameOnDatatable;

	private EntityTables(String name, Integer[] codes, String foreignKeyColumnNameOnDatatable) {
		this.name = name;
		this.codes = codes;
		this.foreignKeyColumnNameOnDatatable = foreignKeyColumnNameOnDatatable;
	}

	public static List<String> getEntitiesList() {

		List<String> data = new ArrayList<String>();

		for (EntityTables entity : EntityTables.values()) {
			data.add(entity.name);
		}

		return data;

	}

	public static Integer[] getStatus(String name) {
		if (lookup.get(name) != null) {
			return lookup.get(name).getCodes();
		}
		return new Integer[]{};
	}

	public Integer[] getCodes() {
		return this.codes;
	}

	public String getName() {
		return name;
	}

	public String getForeignKeyColumnNameOnDatatable() {
		return this.foreignKeyColumnNameOnDatatable;
	}

	public static String getForeignKeyColumnNameOnDatatable(String name) {
		return lookup.get(name).foreignKeyColumnNameOnDatatable;
	}

}
