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
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.data.MixTaxonomyMappingData;
import org.apache.fineract.mix.data.XBRLData;
import org.apache.fineract.mix.exception.XBRLMappingInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class XBRLResultServiceImpl implements XBRLResultService {

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("JavaScript");

    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final MixTaxonomyReadPlatformService readTaxonomyService;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public XBRLResultServiceImpl(final JdbcTemplate jdbcTemplate, final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService,
            final MixTaxonomyReadPlatformService readTaxonomyService) {
        this.readTaxonomyMappingService = readTaxonomyMappingService;
        this.readTaxonomyService = readTaxonomyService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public XBRLData getXBRLResult(final Date startDate, final Date endDate, final String currency) {

        final HashMap<MixTaxonomyData, BigDecimal> config = retrieveTaxonomyConfig(startDate, endDate);
        if (config == null || config.size() == 0) {
            throw new XBRLMappingInvalidException("Mapping is empty");
        }
        return new XBRLData().setResultMap(config).setStartDate(startDate).setEndDate(endDate).setCurrency(currency);
    }

    @SuppressWarnings("unchecked")
    private HashMap<MixTaxonomyData, BigDecimal> retrieveTaxonomyConfig(final Date startDate, final Date endDate) {
        final MixTaxonomyMappingData taxonomyMapping = this.readTaxonomyMappingService.retrieveTaxonomyMapping();
        if (taxonomyMapping == null) {
            return null;
        }
        final String config = taxonomyMapping.getConfig();
        if (config != null) {
            // <taxonomyId, mapping>
            HashMap<String, String> configMap = new HashMap<>();
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

    private String getAccountSql(final Date startDate, final Date endDate) {
        return "select debits.glcode as 'glcode', debits.name as 'name', coalesce(debits.debitamount,0)-coalesce(credits.creditamount,0)) as 'balance' "
                + "from (select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'debitamount' "
                + "from acc_gl_journal_entry,acc_gl_account " + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=2 " + "and acc_gl_journal_entry.entry_date <= " + endDate
                + " and acc_gl_journal_entry.entry_date > " + startDate
                // "and (acc_gl_journal_entry.office_id=${branch} or
                // ${branch}=1) "
                // +
                + " group by glcode " + "order by glcode) debits " + "LEFT OUTER JOIN "
                + "(select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'creditamount' "
                + "from acc_gl_journal_entry,acc_gl_account " + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=1 " + "and acc_gl_journal_entry.entry_date <= " + endDate
                + " and acc_gl_journal_entry.entry_date > " + startDate
                // "and (acc_gl_journal_entry.office_id=${branch} or
                // ${branch}=1) "
                // +
                + " group by glcode " + "order by glcode) credits " + "on debits.glcode=credits.glcode " + "union "
                + "select credits.glcode as 'glcode', credits.name as 'name', coalesce(debits.debitamount,0)-coalesce(credits.creditamount,0)) as 'balance' "
                + "from (select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'debitamount' "
                + "from acc_gl_journal_entry,acc_gl_account " + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=2 " + "and acc_gl_journal_entry.entry_date <= " + endDate
                + " and acc_gl_journal_entry.entry_date > " + startDate
                // "and (acc_gl_journal_entry.office_id=${branch} or
                // ${branch}=1) "
                // +
                + " group by glcode " + "order by glcode) debits " + "RIGHT OUTER JOIN "
                + "(select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'creditamount' "
                + "from acc_gl_journal_entry,acc_gl_account " + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=1 " + "and acc_gl_journal_entry.entry_date <= " + endDate
                + " and acc_gl_journal_entry.entry_date > " + startDate
                // "and (acc_gl_journal_entry.office_id=${branch} or
                // ${branch}=1) "
                // +
                + " group by name, glcode " + "order by glcode) credits " + "on debits.glcode=credits.glcode;";
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
