package org.mifosng.platform.noncore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;

import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReadWriteNonCoreDataServiceImpl implements
		ReadWriteNonCoreDataService {

	private final PlatformSecurityContext context;

	private final static Logger logger = LoggerFactory
			.getLogger(ReadWriteNonCoreDataServiceImpl.class);

	@Autowired
	public ReadWriteNonCoreDataServiceImpl(final PlatformSecurityContext context) {
		this.context = context;
	}

	@Autowired
	private GenericDataService genericDataService;

	@Override
	public List<AdditionalFieldsSetData> retrieveExtraDatasetNames(String type) {

		long startTime = System.currentTimeMillis();
		List<AdditionalFieldsSetData> additionalFieldsSets = new ArrayList<AdditionalFieldsSetData>();

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

		sql = genericDataService.wrapSQL(sql);
		String sqlErrorMsg = "Additional Fields Type: " + type + "   sql: "
				+ sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		try {
			while (rs.next()) {
				additionalFieldsSets.add(new AdditionalFieldsSetData(rs
						.getInt("id"), rs.getString("set"), rs
						.getString("type")));
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING retrieveExtraDatasetNames:      Elapsed Time: "
				+ elapsed);

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

		CachedRowSet columnDefinitions = getAdditionalFieldsMetaData(type, set);

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + "   Id: " + id;
		List<ResultsetColumnHeader> columnHeaders = getResultsetColumnHeaders(
				columnDefinitions, sqlErrorMsg);

		String selectFieldList = getSelectFieldListFromColumnHeaders(columnHeaders);

		String unScopedSQL = "select " + selectFieldList + " from `" + type
				+ "` t ${dataScopeCriteria} left join `"
				+ getFullDatasetName(type, set) + "` s on s.id = t.id "
				+ " where t.id = " + id;
		String sql = dataScopedSQL(unScopedSQL, type);
		logger.info("addition fields sql: " + sql);
		
		List<ResultsetDataRow> resultsetDataRows = getResultsetDataRows(
				columnHeaders, sql, sqlErrorMsg);

		return new GenericResultsetData(columnHeaders, resultsetDataRows);

	}

	private String getSelectFieldListFromColumnHeaders(
			List<ResultsetColumnHeader> columnHeaders) {

		Boolean firstColumn = true;
		String selectFieldList = "";
		String selectFieldSeparator = "";

		for (ResultsetColumnHeader columnHeader : columnHeaders) {
			if (firstColumn) {
				selectFieldSeparator = " ";
				firstColumn = false;
			} else {
				selectFieldSeparator = ", ";
			}
			selectFieldList += selectFieldSeparator + "s.`"
					+ columnHeader.getColumnName() + "`";
		}
		return selectFieldList;
	}

	private CachedRowSet getAdditionalFieldsMetaData(String type, String set) {
		String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.allowed_list_id "
				+ " from stretchydata_datasettype t "
				+ " join stretchydata_dataset d on d.datasettype_id = t.id "
				+ " join stretchydata_dataset_fields f on f.dataset_id = d.id "
				+ " where d.`name` = '"
				+ set
				+ "' and t.`name` = '"
				+ type
				+ "' order by f.id";

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + " - SQL: " + sql;

		CachedRowSet columnDefinitions = genericDataService.getCachedResultSet(
				sql, sqlErrorMsg);
		if (columnDefinitions.size() > 0)
			return columnDefinitions;

		throw new PlatformDataIntegrityException("error.msg.sql.error",
				"Additional Fields Meta Data Not Found - " + sqlErrorMsg);
	}

	private List<ResultsetColumnHeader> getResultsetColumnHeaders(
			CachedRowSet columnDefinitions, String sqlErrorMsg) {

		List<ResultsetColumnHeader> columnHeaders = null;

		try {

			columnHeaders = new ArrayList<ResultsetColumnHeader>();
			Integer allowedListId;
			while (columnDefinitions.next()) {
				ResultsetColumnHeader rsch = new ResultsetColumnHeader();
				rsch.setColumnName(columnDefinitions.getString("name"));

				rsch.setColumnType(columnDefinitions.getString("data_type"));
				if (columnDefinitions.getInt("data_length") > 0)
					rsch.setColumnLength(columnDefinitions
							.getInt("data_length"));

				rsch.setColumnDisplayType(columnDefinitions
						.getString("display_type"));
				allowedListId = columnDefinitions.getInt("allowed_list_id");

				if (allowedListId > 0) {

					String sql = "select v.`name` from stretchydata_allowed_value v where allowed_list_id = "
							+ allowedListId + " order by id";
					CachedRowSet rsValues = genericDataService
							.getCachedResultSet(sql, "SQL: " + sql);
					while (rsValues.next()) {
						rsch.getColumnValues().add(rsValues.getString("name"));
					}
				}
				columnHeaders.add(rsch);
			}
			;
			return columnHeaders;

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

	}

	private List<ResultsetDataRow> getResultsetDataRows(
			List<ResultsetColumnHeader> columnHeaders, String sql,
			String sqlErrorMsg) {

		List<ResultsetDataRow> resultsetDataRows = null;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		if (rs.size() != 1)
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					"Expected One Entry to be Returned But " + rs.size()
							+ " were Found - " + sqlErrorMsg);

		String columnName = null;
		String columnValue = null;
		resultsetDataRows = new ArrayList<ResultsetDataRow>();
		ResultsetDataRow resultsetDataRow;

		try {
			while (rs.next()) {
				resultsetDataRow = new ResultsetDataRow();
				List<String> columnValues = new ArrayList<String>();

				for (int i = 0; i < columnHeaders.size(); i++) {
					columnName = columnHeaders.get(i).getColumnName();
					columnValue = rs.getString(columnName);
					columnValues.add(columnValue);
				}
				resultsetDataRow.setRow(columnValues);
				resultsetDataRows.add(resultsetDataRow);
			}
			return resultsetDataRows;

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

	}

	@Override
	public void updateExtraData(String type, String set, Long id,
			Map<String, String> queryParams) {

		long startTime = System.currentTimeMillis();

		checkResourceTypeThere(type, set);
		String fullDatasetName = getFullDatasetName(type, set);
		String transType = getTransType(type, fullDatasetName, id);

		String saveSql = getSaveSql(fullDatasetName, id, transType, queryParams);

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + "   Id: " + id;
		genericDataService.updateSQL(saveSql, sqlErrorMsg);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING updateExtraData:      Elapsed Time: " + elapsed
				+ "       - type: " + type + "    set: " + set + "  id: " + id);
	}

	private void checkResourceTypeThere(String type, String set) {

		String sql = "select t.id from stretchydata_datasettype t join stretchydata_dataset d on d.datasettype_id = t.id where d.`name` = '"
				+ set + "' and t.`name` = '" + type + "'";

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + "   Sql: " + sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		try {

			if (!(rs.next())) {
				throw new AdditionalFieldsNotFoundException(type, set);
			}

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

	}

	private String getTransType(String type, String fullDatasetName, Long id) {

		String sql = "select s.id from `" + type + "` t left join `"
				+ fullDatasetName + "` s on s.id = t.id where t.id = " + id;

		String sqlErrorMsg = "Additional Fields Type: " + type
				+ "   Full Set Name: " + fullDatasetName + "   Sql: " + sql;
		CachedRowSet rs = genericDataService.getCachedResultSet(sql,
				sqlErrorMsg);

		try {

			if (rs.next()) {
				String idValue = rs.getString("id");
				if (idValue != null)
					return "E"; // update (edit)

				return "A"; // create (add)
			}
			throw new AdditionalFieldsNotFoundException(type, id);

		} catch (SQLException e) {
			throw new PlatformDataIntegrityException("error.msg.sql.error",
					e.getMessage(), sqlErrorMsg);
		}

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

}