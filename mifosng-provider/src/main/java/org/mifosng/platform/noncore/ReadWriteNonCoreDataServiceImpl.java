package org.mifosng.platform.noncore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.rowset.CachedRowSetImpl;

@Service
public class ReadWriteNonCoreDataServiceImpl implements
		ReadWriteNonCoreDataService {

	private final PlatformSecurityContext context;

	private final static Logger logger = LoggerFactory
			.getLogger(ReadWriteNonCoreDataServiceImpl.class);

	private final DataSource dataSource;

	@Autowired
	public ReadWriteNonCoreDataServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {

		this.context = context;
		this.dataSource = dataSource;
	}

	@Autowired
	private GenericDataService genericDataService;

	@Override
	public List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type) {

		List<AdditionalFieldsSetData> additionalFieldsSets = new ArrayList<AdditionalFieldsSetData>();

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();

			String andClause;
			if (type == null) {
				andClause = "";
			} else {
				andClause = " and t.`name` = '" + type + "'";
			}
			// PERMITTED ADDITIONAL FIELDS datasets
			String sql = "select d.id, d.`name` as 'set', t.`name` as 'type' "
					+ " from stretchydata_dataset d join stretchydata_datasettype t on t.id = d.datasettype_id "
					+ " where exists"
					+ " (select 'f'"
					+ " from m_appuser_role ur "
					+ " join m_role r on r.id = ur.role_id"
					+ " left join m_role_permission rp on rp.role_id = r.id"
					+ " left join m_permission p on p.id = rp.permission_id"
					+ " where ur.appuser_id = "
					+ context.authenticatedUser().getId()
					+ " and (r.name = 'Super User' or r.name = 'Read Only') or p.code = concat('CAN_READ_', t.`name`, '_x', d.`name`)) "
					+ andClause + " order by d.`name`";

			ResultSet rs = db_statement.executeQuery(sql);

			while (rs.next()) {
				additionalFieldsSets.add(new AdditionalFieldsSetData(rs
						.getInt("id"), rs.getString("set"), rs
						.getString("type")));
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type);
		} finally {
			genericDataService.dbClose(db_statement, db_connection);
		}

		return additionalFieldsSets;
	}

	@Override
	public GenericResultsetData retrieveExtraData(String type, String set,
			Long id) {

		long startTime = System.currentTimeMillis();
		GenericResultsetData result = fillExtraDataGenericResultSet(type, set,
				id);
		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING SET: " + set + "     Elapsed Time: " + elapsed);
		return result;
	}

	private GenericResultsetData fillExtraDataGenericResultSet(
			final String type, final String set, final Long id) {

		Connection db_connection = null;
		// Statement db_statement1 = null;
		Statement db_statement2 = null;
		Statement db_statement3 = null;
		try {

			/*
			 * while (crs.next()) { logger.info("Cached - Name: " +
			 * crs.getString("name") + "    Data Type: " +
			 * crs.getString("data_type") + "    Data Length: " +
			 * crs.getString("data_length") + "    Display Type: " +
			 * crs.getString("display_type") + "    allowed_list_id: " +
			 * crs.getString("allowed_list_id")); }
			 */

			db_connection = dataSource.getConnection();
			// db_statement1 = db_connection.createStatement();
			String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.allowed_list_id from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id join stretchydata_dataset_fields f on f.dataset_id = d.id where d.`name` = '"
					+ set + "' and t.`name` = '" + type + "' order by f.id";

			String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
					+ set + "   Id: " + id;
			CachedRowSetImpl rsmd = getCachedResultSet(sql, sqlErrorMsg);
			// ResultSet rsmd = db_statement1.executeQuery(sql);

			List<ResultsetColumnHeader> columnHeaders = null;
			List<ResultsetDataRow> resultsetDataRows = null;

			if (rsmd.next()) {

				String fullDatasetName = getFullDatasetName(type, set);
				db_statement2 = db_connection.createStatement();
				columnHeaders = new ArrayList<ResultsetColumnHeader>();
				Boolean firstColumn = true;
				Integer allowedListId;
				String selectFieldList = "";
				String selectFieldSeparator = "";
				do {
					ResultsetColumnHeader rsch = new ResultsetColumnHeader();
					rsch.setColumnName(rsmd.getString("name"));

					if (firstColumn) {
						selectFieldSeparator = " ";
						firstColumn = false;
					} else {
						selectFieldSeparator = ", ";
					}
					selectFieldList += selectFieldSeparator + "s.`"
							+ rsch.getColumnName() + "`";

					rsch.setColumnType(rsmd.getString("data_type"));
					if (rsmd.getInt("data_length") > 0)
						rsch.setColumnLength(rsmd.getInt("data_length"));
					
					rsch.setColumnDisplayType(rsmd.getString("display_type"));
					allowedListId = rsmd.getInt("allowed_list_id");
					
					if (allowedListId > 0) {

						// logger.info("allowedListId != null: Column: " +
						// rsch.getColumnName());
						sql = "select v.`name` from stretchydata_allowed_value v where allowed_list_id = "
								+ allowedListId + " order by id";
						ResultSet rsValues = db_statement2.executeQuery(sql);
						while (rsValues.next()) {
							rsch.getColumnValues().add(
									rsValues.getString("name"));
						}
					}
					columnHeaders.add(rsch);
				} while (rsmd.next());

				String unScopedSQL = "select " + selectFieldList + " from `"
						+ type + "` t ${dataScopeCriteria} left join `"
						+ fullDatasetName + "` s on s.id = t.id "
						+ " where t.id = " + id;

				sql = dataScopedSQL(unScopedSQL, type);

				logger.info("addition fields sql: " + sql);
				db_statement3 = db_connection.createStatement();
				ResultSet rs = db_statement3.executeQuery(sql);

				if (rs.next()) {
					String columnName = null;
					String columnValue = null;
					resultsetDataRows = new ArrayList<ResultsetDataRow>();
					ResultsetDataRow resultsetDataRow;
					do {
						resultsetDataRow = new ResultsetDataRow();
						List<String> columnValues = new ArrayList<String>();

						for (int i = 0; i < columnHeaders.size(); i++) {
							columnName = columnHeaders.get(i).getColumnName();
							columnValue = rs.getString(columnName);
							columnValues.add(columnValue);
						}
						resultsetDataRow.setRow(columnValues);
						resultsetDataRows.add(resultsetDataRow);
					} while (rs.next());
				} else {
					// could also be not found because of data scope
					throw new AdditionalFieldsNotFoundException(type, id);
				}
			} else {
				throw new AdditionalFieldsNotFoundException(type, set);
			}

			return new GenericResultsetData(columnHeaders, resultsetDataRows);

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Id: " + id);
		} finally {

			genericDataService.dbClose(db_statement2, null);
			genericDataService.dbClose(db_statement3, db_connection);
		}
	}

	@Override
	public void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams) {
		logger.info("updateExtraData - type: " + type + "    set: " + set
				+ "  id: " + id);

		checkResourceTypeThere(type, set);
		String fullDatasetName = getFullDatasetName(type, set);
		String transType = getTransType(type, fullDatasetName, id);

		String saveSql = getSaveSql(fullDatasetName, id, transType, queryParams);

		logger.info("saveSQL: " + saveSql);
		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			db_statement.executeUpdate(saveSql);

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Id: " + id);
		} finally {
			genericDataService.dbClose(db_statement, db_connection);
		}

	}

	private void checkResourceTypeThere(String type, String set) {

		String sql = "select 'f' from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id where d.`name` = '"
				+ set + "' and t.`name` = '" + type + "'";

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			if (!(rs.next())) {
				throw new AdditionalFieldsNotFoundException(type, set);
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Set: " + set + "   Sql: " + sql);
		} finally {
			genericDataService.dbClose(db_statement, db_connection);
		}

	}

	private String getTransType(String type, String fullDatasetName, Long id) {
		String transType = null;

		String sql = "select s.id from `" + type + "` t left join `"
				+ fullDatasetName + "` s on s.id = t.id where t.id = " + id;

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			if (rs.next()) {
				String idValue = rs.getString("id");
				if (idValue != null) {
					transType = "E";
				} else {
					transType = "A";
				}
			} else {
				throw new AdditionalFieldsNotFoundException(type, id);
			}
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), "Additional Fields Type: " + type
							+ "   Full Set Name: " + fullDatasetName
							+ "   Sql: " + sql);
		} finally {
			genericDataService.dbClose(db_statement, db_connection);
		}

		return transType;
	}

	private String getSaveSql(String fullSetName, Long id, String transType,
			Map<String, String> queryParams) {

		String pValue = "";
		String pValueWrite = "";
		String saveSql = "";
		String singleQuote = "'";
		String underscore = "_";
		String space = " ";
		Set<String> keys = queryParams.keySet();

		if (transType.equals("E")) {
			boolean firstColumn = true;
			saveSql = "update `" + fullSetName + "` ";

			for (String key : keys) {
				if (!(key.equalsIgnoreCase("id"))) {
					if (firstColumn) {
						saveSql += " set ";
						firstColumn = false;
					} else {
						saveSql += ", ";
					}

					pValue = queryParams.get(key);
					if (pValue == null || pValue.equals("")) {
						pValueWrite = "null";
					} else {
						pValueWrite = singleQuote
								+ genericDataService.replace(pValue,
										singleQuote, singleQuote + singleQuote)
								+ singleQuote;
					}
					saveSql += "`"
							+ genericDataService
									.replace(key, underscore, space) + "` = "
							+ pValueWrite;
				}
			}

			saveSql += " where id = " + id;
		} else {
			String insertColumns = "";
			String selectColumns = "";
			String columnName = "";
			for (String key : keys) {
				pValue = queryParams.get(key);
				if (!(key.equalsIgnoreCase("id"))) {

					pValue = queryParams.get(key);
					if (pValue == null || pValue.equals("")) {
						pValueWrite = "null";
					} else {
						pValueWrite = singleQuote
								+ genericDataService.replace(pValue,
										singleQuote, singleQuote + singleQuote)
								+ singleQuote;
					}
					columnName = "`"
							+ genericDataService
									.replace(key, underscore, space) + "`";
					insertColumns += ", " + columnName;
					selectColumns += "," + pValueWrite + " as " + columnName;
				}
			}

			saveSql = "insert into `" + fullSetName + "` (id" + insertColumns
					+ ")" + " select " + id + " as id" + selectColumns;
		}
		return saveSql;
	}

	private String dataScopedSQL(String unScopedSQL, String type) {
		String dataScopeCriteria = null;
		/*
		 * unfortunately have to, one way or another, be able to restrict data
		 * to the users office hierarchy. Here it's hardcoded for client and
		 * loan. They are the main application tables. But if additional fields
		 * are needed on other tables like group, loan_transaction or others the
		 * same applies (hardcoding of some sort)
		 */

		AppUser currentUser = context.authenticatedUser();
		if (type.equalsIgnoreCase("m_client")) {
			dataScopeCriteria = " join m_office o on o.id = t.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}
		if (type.equalsIgnoreCase("m_loan")) {
			dataScopeCriteria = " join m_client c on c.id = t.client_id "
					+ " join m_office o on o.id = c.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}

		if (dataScopeCriteria == null) {
			throw new PlatformDataIntegrityException(
					"error.msg.invalid.dataScopeCriteria", "Type: " + type
							+ " not catered for in data Scoping");
		}

		return genericDataService.replace(unScopedSQL, "${dataScopeCriteria}",
				dataScopeCriteria);

	}

	private String getFullDatasetName(final String type, final String set) {
		return type + "_x" + set;
	}

	@Override
	public String retrieveDataTable(String datatable, String sqlFields,
			String sqlSearch, String sqlOrder) {
		long startTime = System.currentTimeMillis();

		String sql = "select ";
		if (sqlFields != null)
			sql = sql + sqlFields;
		else
			sql = sql + " * ";

		sql = sql + " from " + datatable;

		if (sqlSearch != null)
			sql = sql + " where " + sqlSearch;
		if (sqlOrder != null)
			sql = sql + " order by " + sqlOrder;

		GenericResultsetData result = genericDataService
				.fillGenericResultSet(sql);

		String jsonString = generateJsonFromGenericResultsetData(result);

		logger.info("JSON is: " + jsonString);
		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING DATATABLE: " + datatable + "     Elapsed Time: "
				+ elapsed);
		return jsonString;
	}

	private static String generateJsonFromGenericResultsetData(
			GenericResultsetData result) {

		StringBuffer writer = new StringBuffer();

		writer.append("[");

		List<ResultsetColumnHeader> columnHeaders = result.getColumnHeaders();
		logger.info("NO. of Columns: " + columnHeaders.size());

		List<ResultsetDataRow> data = result.getData();
		List<String> row;
		Integer rSize;
		String currColType;
		String currVal;
		logger.info("NO. of Rows: " + data.size());
		for (int i = 0; i < data.size(); i++) {
			writer.append("\n{");

			row = data.get(i).getRow();
			rSize = row.size();
			for (int j = 0; j < rSize; j++) {

				writer.append('\"' + columnHeaders.get(j).getColumnName()
						+ '\"' + ": ");
				currColType = columnHeaders.get(j).getColumnType();
				currVal = row.get(j);
				if (currVal != null) {
					if (currColType.equals("DECIMAL")
							|| currColType.equals("DOUBLE")
							|| currColType.equals("BIGINT")
							|| currColType.equals("SMALLINT")
							|| currColType.equals("INT"))
						writer.append(currVal);
					else
						writer.append('\"' + currVal + '\"');
				} else
					writer.append("null");

				if (j < (rSize - 1))
					writer.append(",\n");
			}

			if (i < (data.size() - 1))
				writer.append("},");
			else
				writer.append("}");
		}

		writer.append("\n]");
		return writer.toString();

		/*
		 * JSONObject js = null; try { js = new JSONObject(writer.toString()); }
		 * catch (JSONException e) { throw new WebApplicationException(Response
		 * .status(Status.BAD_REQUEST).entity("JSON body is wrong") .build()); }
		 * 
		 * return js.toString();
		 */

	}

	private CachedRowSetImpl getCachedResultSet(String sql, String errorMsg) {

		Connection db_connection = null;
		Statement db_statement = null;
		try {
			db_connection = dataSource.getConnection();
			db_statement = db_connection.createStatement();
			ResultSet rs = db_statement.executeQuery(sql);

			CachedRowSetImpl crs = new CachedRowSetImpl();
			crs.populate(rs);
			return crs;
		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), errorMsg);
		} finally {
			genericDataService.dbClose(db_statement, db_connection);

		}

	}
}