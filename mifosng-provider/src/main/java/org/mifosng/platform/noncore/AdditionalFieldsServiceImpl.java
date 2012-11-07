package org.mifosng.platform.noncore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.CachedRowSet;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.data.ResultsetColumnHeader;
import org.mifosng.platform.api.data.ResultsetDataRow;
import org.mifosng.platform.exceptions.AdditionalFieldsNotFoundException;
import org.mifosng.platform.exceptions.DataTableNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//TODO - this service is deprecated and will be replaced by the data tables functionality
@Service
public class AdditionalFieldsServiceImpl implements AdditionalFieldsService {

	private final PlatformSecurityContext context;

	private final static Logger logger = LoggerFactory
			.getLogger(AdditionalFieldsServiceImpl.class);

	@Autowired
	public AdditionalFieldsServiceImpl(final PlatformSecurityContext context) {
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
				+ " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', t.`name`, '_x', d.`name`))) "
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

		checkMainResourceExistsWithinScope(type, id);

		CachedRowSet columnDefinitions = getAdditionalFieldsMetaData(type, set);

		String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
				+ set + "   Id: " + id;
		List<ResultsetColumnHeader> columnHeaders = getResultsetColumnHeaders(
				columnDefinitions, sqlErrorMsg);

		String selectFieldList = getSelectFieldListFromColumnHeaders(columnHeaders);

		String sql = "select " + selectFieldList + " from `" + type
				+ "` t left join `" + getFullDatasetName(type, set)
				+ "` s on s.id = t.id where t.id = " + id;
		logger.info("addition fields sql: " + sql);

		List<ResultsetDataRow> resultsetDataRows = getResultsetDataRows(
				columnHeaders, sql, sqlErrorMsg);

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING SET: " + set + "     Elapsed Time: " + elapsed);

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
		String sql = "select f.`name`, f.data_type, f.data_length, f.display_type, f.code_id "
				+ " from stretchydata_datasettype t "
				+ " join stretchydata_dataset d on d.datasettype_id = t.id "
				+ " join stretchydata_dataset_fields f on f.dataset_id = d.id "
				+ " where d.`name` = '"
				+ set
				+ "' and t.`name` = '"
				+ type
				+ "' order by f.id";

		CachedRowSet columnDefinitions = genericDataService.getCachedResultSet(
				sql, "SQL: " + sql);

		if (columnDefinitions.size() > 0)
			return columnDefinitions;

		throw new AdditionalFieldsNotFoundException(type, set);
	}

	private List<ResultsetColumnHeader> getResultsetColumnHeaders(
			CachedRowSet columnDefinitions, String sqlErrorMsg) {

		List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
		ResultsetColumnHeader rschId = new ResultsetColumnHeader();
		rschId.setColumnName("id");
		rschId.setColumnType("Integer");
		columnHeaders.add(rschId);

		try {

			Integer codeId;
			while (columnDefinitions.next()) {
				ResultsetColumnHeader rsch = new ResultsetColumnHeader();
				rsch.setColumnName(columnDefinitions.getString("name"));

				rsch.setColumnType(columnDefinitions.getString("data_type"));
				if (columnDefinitions.getInt("data_length") > 0)
					rsch.setColumnLength(columnDefinitions
							.getLong("data_length"));

				rsch.setColumnDisplayType(columnDefinitions
						.getString("display_type"));
				codeId = columnDefinitions.getInt("code_id");

				if (codeId > 0) {
					String sql = "select code_value from m_code_value where code_id = "
							+ codeId + " order by order_position, id";
					CachedRowSet rsValues = genericDataService
							.getCachedResultSet(sql, "SQL: " + sql);
					while (rsValues.next()) {
						rsch.getColumnValues().add(
								rsValues.getString("code_value"));
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

		GenericResultsetData readResultset = retrieveExtraData(type, set, id);

		String idValue = readResultset.getData().get(0).getRow().get(0);
		String transType = "E";
		if (idValue == null)
			transType = "A";

		String fullDatasetName = getFullDatasetName(type, set);

		String saveSql = getSaveSql(readResultset, fullDatasetName, id,
				transType, queryParams);

		if (saveSql != null) {
			String sqlErrorMsg = "Additional Fields Type: " + type + "   Set: "
					+ set + "   Id: " + id;
			genericDataService.updateSQL(saveSql, sqlErrorMsg);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		logger.info("FINISHING updateExtraData:      Elapsed Time: " + elapsed
				+ "       - type: " + type + "    set: " + set + "  id: " + id);
	}

	private String getSaveSql(GenericResultsetData readResultset,
			String fullSetName, Long id, String transType,
			Map<String, String> queryParams) {

		Set<String> keys = queryParams.keySet();

		String underscore = "_";
		String space = " ";
		String pValue = null;
		String keyUpdated = null;
		List<ResultsetColumnHeader> columnHeaders = readResultset
				.getColumnHeaders();
		List<String> columnValues = readResultset.getData().get(0).getRow();

		Map<String, String> updatedColumns = new HashMap<String, String>();

		for (String key : keys) {
			if (!(key.equalsIgnoreCase("id"))) {
				keyUpdated = genericDataService.replace(key, underscore, space);
				pValue = queryParams.get(key);

				if (newValueValidAndChanged(keyUpdated, columnHeaders,
						columnValues, pValue))
					updatedColumns.put(keyUpdated, pValue);

			}
		}

		// just updating fields that have changed since pre-update read - though
		// its possible these values are different from the page the user was
		// looking at and even different from the current db values (if some
		// other update got in quick) - would need a version field for
		// completeness but its okay to take this risk with additional fields
		// data

		if (updatedColumns.size() == 0)
			return null;

		String pValueWrite = "";
		String saveSql = "";
		String singleQuote = "'";

		if (transType.equals("E")) {
			boolean firstColumn = true;
			saveSql = "update `" + fullSetName + "` ";

			for (String key : updatedColumns.keySet()) {
				if (firstColumn) {
					saveSql += " set ";
					firstColumn = false;
				} else {
					saveSql += ", ";
				}

				pValue = updatedColumns.get(key);
				if (StringUtils.isEmpty(pValue)) {
					pValueWrite = "null";
				} else {
					pValueWrite = singleQuote
							+ genericDataService.replace(pValue, singleQuote,
									singleQuote + singleQuote) + singleQuote;
				}
				saveSql += "`" + key + "` = " + pValueWrite;
			}

			saveSql += " where id = " + id;
		} else {
			String insertColumns = "";
			String selectColumns = "";
			String columnName = "";
			for (String key : updatedColumns.keySet()) {
				pValue = updatedColumns.get(key);

				if (StringUtils.isEmpty(pValue)) {
					pValueWrite = "null";
				} else {
					pValueWrite = singleQuote
							+ genericDataService.replace(pValue, singleQuote,
									singleQuote + singleQuote) + singleQuote;
				}
				columnName = "`" + key + "`";
				insertColumns += ", " + columnName;
				selectColumns += "," + pValueWrite + " as " + columnName;
			}

			saveSql = "insert into `" + fullSetName + "` (id" + insertColumns
					+ ")" + " select " + id + " as id" + selectColumns;
		}
		return saveSql;
	}

	private boolean notTheSame(String currValue, String pValue) {
		if (StringUtils.isEmpty(currValue) && StringUtils.isEmpty(pValue))
			return false;

		if (StringUtils.isEmpty(currValue))
			return true;

		if (StringUtils.isEmpty(pValue))
			return true;

		if (currValue.equals(pValue))
			return false;

		return true;
	}

	private boolean newValueValidAndChanged(String key,
			List<ResultsetColumnHeader> columnHeaders,
			List<String> columnValues, String newValue) {

		String columnValue = null;
		for (int i = 0; i < columnHeaders.size(); i++) {
			if (columnHeaders.get(i).getColumnName().equalsIgnoreCase(key)) {
				columnValue = columnValues.get(i);

				if ((!StringUtils.isEmpty(newValue))
						&& columnHeaders.get(i).getColumnValues().size() > 0) {
					if (!(columnHeaders.get(i).getColumnValues()
							.contains(newValue))) {
						throw new PlatformDataIntegrityException(
								"error.msg.invalid.columnValue",
								"Parameter Column Name: " + key + " - Value '"
										+ newValue
										+ "' not found in Allowed Value list");
					}
				}

				if (notTheSame(columnValue, newValue)) {
					logger.info("Difference - Column: " + key
							+ "- Current Value: " + columnValue
							+ "    New Value: " + newValue);
					return true;

				}
				return false;
			}
		}

		throw new PlatformDataIntegrityException(
				"error.msg.invalid.columnName", "Parameter Column Name: " + key
						+ " not found");
	}

	private String getFullDatasetName(final String type, final String set) {
		return type + "_x" + set;
	}

	// only exclusively datatable functions below here
	private void checkMainResourceExistsWithinScope(String appTable,
			Long appTableId) {

		String unscopedSql = "select t.id from " + appTable
				+ " t ${dataScopeCriteria} where t.id = " + appTableId;

		String sql = dataScopedSQL(unscopedSql, appTable);

		CachedRowSet rs = genericDataService.getCachedResultSet(sql, "SQL : "
				+ sql);

		if (rs.size() == 0)
			throw new DataTableNotFoundException(appTable, appTableId);
	}

	private String dataScopedSQL(String unscopedSQL, String appTable) {
		String dataScopeCriteria = null;
		/*
		 * unfortunately have to, one way or another, be able to restrict data
		 * to the users office hierarchy. Here it's hardcoded for client and
		 * loan. They are the main application tables. But if additional fields
		 * are needed on other tables like group, loan_transaction or others the
		 * same applies (hardcoding of some sort)
		 */

		AppUser currentUser = context.authenticatedUser();
		if (appTable.equalsIgnoreCase("m_client")) {
			dataScopeCriteria = " join m_office o on o.id = t.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}
		if (appTable.equalsIgnoreCase("m_loan")) {
			dataScopeCriteria = " join m_client c on c.id = t.client_id "
					+ " join m_office o on o.id = c.office_id and o.hierarchy like '"
					+ currentUser.getOffice().getHierarchy() + "%'";
		}

		if (dataScopeCriteria == null) {
			throw new PlatformDataIntegrityException(
					"error.msg.invalid.dataScopeCriteria",
					"Application Table: " + appTable
							+ " not catered for in data Scoping");
		}

		return genericDataService.replace(unscopedSQL, "${dataScopeCriteria}",
				dataScopeCriteria);

	}

}