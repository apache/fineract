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
package org.apache.fineract.infrastructure.security.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

@Component
public class ColumnValidator {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ColumnValidator(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private void validateColumn(Map<String, Set<String>> tableColumnMap) {
		Connection connection = null ;
		try {
			connection = this.jdbcTemplate.getDataSource()
					.getConnection() ;
			DatabaseMetaData dbMetaData = connection.getMetaData();
			ResultSet resultSet = null;
			for (HashMap.Entry<String, Set<String>> entry : tableColumnMap
					.entrySet()) {
				Set<String> columns = entry.getValue();
				resultSet = dbMetaData.getColumns(null, null, entry.getKey(),
						null);
				Set<String> tableColumns = getTableColumns(resultSet);
				if (columns.size() > 0 && tableColumns.size() == 0) {
					throw new SQLInjectionException();
				}
				for (String requestedColumn : columns) {
					if (!tableColumns.contains(requestedColumn)) {
						throw new SQLInjectionException();
					}
				}
			}
		} catch (SQLException e) {
			throw new SQLInjectionException();
		}finally {
			if(connection != null) {
				DataSourceUtils.releaseConnection(connection, jdbcTemplate.getDataSource());
			}
			connection = null ;
		}
	}

	private Set<String> getTableColumns(final ResultSet rs) {
		Set<String> columns = new HashSet<>();
		try {
			while (rs.next()) {
				columns.add(rs.getString("column_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return columns;
	}

	public void validateSqlInjection(String schema, String... conditions) {
		for(String condition: conditions) {
			SQLInjectionValidator.validateSQLInput(condition);
			List<String> operator = new ArrayList<>(Arrays.asList("=", ">", "<",
					"> =", "< =", "! =", "!=", ">=", "<="));
			condition = condition.trim().replace("( ", "(").replace(" )", ")")
					.toLowerCase();
			for (String op : operator) {
				condition = replaceAll(condition, op).replaceAll(" +", " ");
			}
			Set<String> operands = getOperand(condition);
			schema = schema.trim().replaceAll(" +", " ").toLowerCase();
			Map<String, Set<String>> tableColumnAliasMap = getTableColumnAliasMap(operands);
			Map<String, Set<String>> tableColumnMap = getTableColumnMap(schema,
					tableColumnAliasMap);
			validateColumn(tableColumnMap);
		}
	}

	private static Map<String, Set<String>> getTableColumnMap(String schema,
			Map<String, Set<String>> tableColumnAliasMap) {
		Map<String, Set<String>> tableColumnMap = new HashMap<>();
		schema = schema.substring(schema.indexOf("from"));
		for (String alias : tableColumnAliasMap.keySet()) {
			int index = schema.indexOf(" " + alias + " ");
			if (index > -1) {
				int startPos = 0;
				startPos = schema.substring(0, index - 1).lastIndexOf(' ',
						index);
				Set<String> columns = tableColumnAliasMap.get(alias);
				tableColumnMap.put(schema.substring(startPos, index).trim(),
						columns);
			} else {
				throw new SQLInjectionException();
			}
		}
		return tableColumnMap;
	}

	private static Map<String, Set<String>> getTableColumnAliasMap(
			Set<String> operands) {
		Map<String, Set<String>> tableColumnMap = new HashMap<>();
		for (String operand : operands) {
			String[] tableColumn = operand.split("\\.");
			if (tableColumn.length == 2) {
				if (tableColumnMap.containsKey(tableColumn[0])) {
					Set<String> columns = tableColumnMap.get(tableColumn[0]);
					columns.add(tableColumn[1]);
				} else {
					Set<String> columns = new HashSet<>();
					columns.add(tableColumn[1]);
					tableColumnMap.put(tableColumn[0], columns);
				}
			} else {
				throw new SQLInjectionException();
			}
		}
		return tableColumnMap;
	}

	private static Set<String> getOperand(String condition) {
		Set<String> operandList = new HashSet<>();
		List<String> operatorList = new ArrayList<>(Arrays.asList("!=", "=",
				">", "<", " like ", " between ", " in ", " in(", " is ",
				" is not ", " equals ", " not equals "));
		for (String op : operatorList) {
			int startIndex = 0;
			do {
				int index = condition.indexOf(op, startIndex);
				if (index > -1) {
					char currentChar = condition.charAt(index - 1);
					if (op.equals("=")) {
						if (!((currentChar + "").equals("!")
								|| (currentChar + "").equals(">") || (currentChar + "")
								.equals("<"))) {
							operandList.add(getOperand(condition, index,
									currentChar));
						}
					} else {
						operandList.add(getOperand(condition, index,
								currentChar));
					}

					startIndex = index + op.length();
				}

			} while (condition.indexOf(op, startIndex) > -1);
		}
		return operandList;
	}

	private static String getOperand(String condition, int index,
			char currentChar) {
		int startPos = 0;
		if ((currentChar + "").equals(" ")) {
			startPos = condition.substring(0, index - 1)
					.lastIndexOf(' ', index);
		} else {
			startPos = condition.substring(0, index).lastIndexOf(' ', index);
		}
		String a = condition.substring(startPos == -1 ? 0 : startPos, index);
		return a.trim().replace("(", "").replace(")", "");
	}

	private static String replaceAll(String condition, String op) {
		int startIndex = 0;
		do {
			int index = condition.indexOf(op, startIndex);
			if (index > -1) {
				if (op.equals("=")) {
					if (!((condition.charAt(index - 1) + "").equals("!")
							|| (condition.charAt(index - 1) + "").equals(">") || (condition
							.charAt(index - 1) + "").equals("<"))) {
						condition = condition.replace(op, " " + op + " ");
						return condition;
					}
					startIndex = index + 2 + op.length();

				} else if (op.equals("< =") || op.equals("> =")
						|| op.equals("! =")) {
					condition = condition.replace(op, op.replace(" ", ""));
					return condition;
				} else {
					condition = condition.replace(op, " " + op + " ");
					return condition;
				}
			} else {
				return condition;
			}

		} while (condition.indexOf(op, startIndex) > -1);
		return condition;
	}
}
