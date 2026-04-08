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
package org.apache.fineract.mix.service;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.mix.data.MixReportXBRLData;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.data.MixTaxonomyMappingData;
import org.apache.fineract.mix.exception.MixReportXBRLMappingInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MixReportXBRLResultServiceImpl implements MixReportXBRLResultService {

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("JavaScript");

    private final MixTaxonomyMappingReadService readTaxonomyMappingService;
    private final MixTaxonomyReadService readTaxonomyService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MixReportXBRLResultServiceImpl(final JdbcTemplate jdbcTemplate, final MixTaxonomyMappingReadService readTaxonomyMappingService,
            final MixTaxonomyReadService readTaxonomyService) {
        this.readTaxonomyMappingService = readTaxonomyMappingService;
        this.readTaxonomyService = readTaxonomyService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MixReportXBRLData getXBRLResult(final Date startDate, final Date endDate, final String currency) {

        final Map<MixTaxonomyData, BigDecimal> config = retrieveTaxonomyConfig(startDate, endDate);

        if (config == null || config.isEmpty()) {
            throw new MixReportXBRLMappingInvalidException("Mapping is empty");
        }

        return new MixReportXBRLData().setResultMap(config).setStartDate(startDate).setEndDate(endDate).setCurrency(currency);
    }

    @SuppressWarnings("unchecked")
    private Map<MixTaxonomyData, BigDecimal> retrieveTaxonomyConfig(final Date startDate, final Date endDate) {
        final MixTaxonomyMappingData taxonomyMapping = this.readTaxonomyMappingService.retrieveTaxonomyMapping();
        if (taxonomyMapping == null) {
            return null;
        }
        final String config = taxonomyMapping.getConfig();
        if (config != null) {
            // <taxonomyId, mapping>
            Map<String, String> configMap = new HashMap<>();
            configMap = new Gson().fromJson(config, configMap.getClass());
            if (configMap == null) {
                return null;
            }
            // <taxonomyId, value>
            final HashMap<MixTaxonomyData, BigDecimal> resultMap = new HashMap<>();
            Map<String, BigDecimal> accountBalanceMap = setupBalanceMap(getAccountSql(startDate, endDate));
            for (final Map.Entry<String, String> entry : configMap.entrySet()) {
                final BigDecimal value = processMappingString(accountBalanceMap, entry.getValue());
                final MixTaxonomyData taxonomy = this.readTaxonomyService.retrieveOne(Long.parseLong(entry.getKey()));
                resultMap.put(taxonomy, value);

            }
            return resultMap;
        }
        return null;
    }

    // TODO: this should at least use prepared statements and not just string concatenate the date objects!
    private String getAccountSql(final Date startDate, final Date endDate) {
        return "SELECT debits.glcode AS 'glcode', debits.name AS 'name', COALESCE(debits.debitamount,0)-COALESCE(credits.creditamount,0)) AS 'balance' "
                + "FROM (SELECT acc_gl_account.gl_code AS 'glcode',name,SUM(amount) AS 'debitamount' "
                + "FROM acc_gl_journal_entry,acc_gl_account WHERE acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "AND acc_gl_journal_entry.type_enum=2 AND acc_gl_journal_entry.entry_date <= " + endDate
                + " AND acc_gl_journal_entry.entry_date > " + startDate
                //
                + " GROUP BY glcode ORDER BY glcode) debits LEFT OUTER JOIN "
                + "(SELECT acc_gl_account.gl_code AS 'glcode',name,SUM(amount) AS 'creditamount' "
                + "FROM acc_gl_journal_entry,acc_gl_account WHERE acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "AND acc_gl_journal_entry.type_enum=1 AND acc_gl_journal_entry.entry_date <= " + endDate
                + " AND acc_gl_journal_entry.entry_date > " + startDate
                //
                + " GROUP BY glcode ORDER BY glcode) credits ON debits.glcode=credits.glcode UNION "
                + "SELECT credits.glcode AS 'glcode', credits.name AS 'name', COALESCE(debits.debitamount,0)-COALESCE(credits.creditamount,0)) AS 'balance' "
                + "FROM (SELECT acc_gl_account.gl_code AS 'glcode',name,SUM(amount) AS 'debitamount' "
                + "FROM acc_gl_journal_entry,acc_gl_account WHERE acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "AND acc_gl_journal_entry.type_enum=2 AND acc_gl_journal_entry.entry_date <= " + endDate
                + " AND acc_gl_journal_entry.entry_date > " + startDate
                //
                + " GROUP BY glcode ORDER BY glcode) debits RIGHT OUTER JOIN "
                + "(SELECT acc_gl_account.gl_code AS 'glcode',name,SUM(amount) AS 'creditamount' "
                + "FROM acc_gl_journal_entry,acc_gl_account WHERE acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "AND acc_gl_journal_entry.type_enum=1 AND acc_gl_journal_entry.entry_date <= " + endDate
                + " AND acc_gl_journal_entry.entry_date > " + startDate
                //
                + " GROUP BY name, glcode ORDER BY glcode) credits ON debits.glcode=credits.glcode;";
    }

    private Map<String, BigDecimal> setupBalanceMap(final String sql) {
        Map<String, BigDecimal> accountBalanceMap = new HashMap<>();

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            accountBalanceMap.put(rs.getString("glcode"), rs.getBigDecimal("balance"));
        }

        return accountBalanceMap;
    }

    // Calculate Taxonomy value from expression
    private BigDecimal processMappingString(Map<String, BigDecimal> accountBalanceMap, String mappingString) {
        final List<String> glCodes = getGLCodes(mappingString);
        for (final String glcode : glCodes) {

            final BigDecimal balance = accountBalanceMap.get(glcode);
            mappingString = mappingString.replaceAll("\\{" + glcode + "\\}", balance != null ? balance.toString() : "0");
        }

        // evaluate the expression
        float eval = 0f;
        try {
            // TODO: this doesn't work anymore in modern JVMs!!!!
            final Number value = (Number) SCRIPT_ENGINE.eval(mappingString);
            if (value != null) {
                eval = value.floatValue();
            }
        } catch (final ScriptException e) {
            log.error("Problem occurred in processMappingString function", e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        return BigDecimal.valueOf(eval);
    }

    public List<String> getGLCodes(final String template) {

        final ArrayList<String> placeholders = new ArrayList<>();

        if (template != null) {

            final Pattern p = Pattern.compile("\\{(.*?)\\}");
            final Matcher m = p.matcher(template);

            while (m.find()) { // find next match
                final String match = m.group();
                final String code = match.substring(1, match.length() - 1);
                placeholders.add(code);
            }

        }
        return placeholders;
    }
}
