/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.mix.data.MixTaxonomyData;
import org.mifosplatform.mix.data.MixTaxonomyMappingData;
import org.mifosplatform.mix.data.XBRLData;
import org.mifosplatform.mix.exception.XBRLMappingInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class XBRLResultServiceImpl implements XBRLResultService {

    private static final ScriptEngine SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName("JavaScript");

    private final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService;
    private final MixTaxonomyReadPlatformService readTaxonomyService;
    private final JdbcTemplate jdbcTemplate;
    private HashMap<String, BigDecimal> accountBalanceMap;

    @Autowired
    public XBRLResultServiceImpl(final RoutingDataSource dataSource,
            final MixTaxonomyMappingReadPlatformService readTaxonomyMappingService, final MixTaxonomyReadPlatformService readTaxonomyService) {
        this.readTaxonomyMappingService = readTaxonomyMappingService;
        this.readTaxonomyService = readTaxonomyService;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public XBRLData getXBRLResult(final Date startDate, final Date endDate, final String currency) {

        final HashMap<MixTaxonomyData, BigDecimal> config = retrieveTaxonomyConfig(startDate, endDate);
        if (config == null || config.size() == 0) { throw new XBRLMappingInvalidException("Mapping is empty"); }
        return new XBRLData(config, startDate, endDate, currency);
    }

    @SuppressWarnings("unchecked")
    private HashMap<MixTaxonomyData, BigDecimal> retrieveTaxonomyConfig(final Date startDate, final Date endDate) {
        final MixTaxonomyMappingData taxonomyMapping = this.readTaxonomyMappingService.retrieveTaxonomyMapping();
        if (taxonomyMapping == null) { return null; }
        final String config = taxonomyMapping.getConfig();
        if (config != null) {
            // <taxonomyId, mapping>
            HashMap<String, String> configMap = new HashMap<>();
            configMap = new Gson().fromJson(config, configMap.getClass());
            if (configMap == null) { return null; }
            // <taxonomyId, value>
            final HashMap<MixTaxonomyData, BigDecimal> resultMap = new HashMap<>();
            setupBalanceMap(getAccountSql(startDate, endDate));
            for (final Entry<String, String> entry : configMap.entrySet()) {
                final BigDecimal value = processMappingString(entry.getValue());
                if (value != null) {
                    final MixTaxonomyData taxonomy = this.readTaxonomyService.retrieveOne(Long.parseLong(entry.getKey()));
                    resultMap.put(taxonomy, value);
                }

            }
            return resultMap;
        }
        return null;
    }

    private String getAccountSql(final Date startDate, final Date endDate) {
        final String sql = "select debits.glcode as 'glcode', debits.name as 'name', (ifnull(debits.debitamount,0)-ifnull(credits.creditamount,0)) as 'balance' "
                + "from (select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'debitamount' "
                + "from acc_gl_journal_entry,acc_gl_account "
                + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=2 " + "and acc_gl_journal_entry.entry_date <= "
                + endDate
                + " and acc_gl_journal_entry.entry_date > "
                + startDate
                +
                // "and (acc_gl_journal_entry.office_id=${branch} or ${branch}=1) "
                // +
                " group by glcode "
                + "order by glcode) debits "
                + "LEFT OUTER JOIN "
                + "(select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'creditamount' "
                + "from acc_gl_journal_entry,acc_gl_account "
                + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=1 "
                + "and acc_gl_journal_entry.entry_date <= "
                + endDate
                + " and acc_gl_journal_entry.entry_date > "
                + startDate
                +
                // "and (acc_gl_journal_entry.office_id=${branch} or ${branch}=1) "
                // +
                " group by glcode "
                + "order by glcode) credits "
                + "on debits.glcode=credits.glcode "
                + "union "
                + "select credits.glcode as 'glcode', credits.name as 'name', (ifnull(debits.debitamount,0)-ifnull(credits.creditamount,0)) as 'balance' "
                + "from (select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'debitamount' "
                + "from acc_gl_journal_entry,acc_gl_account "
                + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=2 "
                + "and acc_gl_journal_entry.entry_date <= "
                + endDate
                + " and acc_gl_journal_entry.entry_date > "
                + startDate
                +
                // "and (acc_gl_journal_entry.office_id=${branch} or ${branch}=1) "
                // +
                " group by glcode "
                + "order by glcode) debits "
                + "RIGHT OUTER JOIN "
                + "(select acc_gl_account.gl_code as 'glcode',name,sum(amount) as 'creditamount' "
                + "from acc_gl_journal_entry,acc_gl_account "
                + "where acc_gl_account.id = acc_gl_journal_entry.account_id "
                + "and acc_gl_journal_entry.type_enum=1 "
                + "and acc_gl_journal_entry.entry_date <= "
                + endDate
                + " and acc_gl_journal_entry.entry_date > " + startDate +
                // "and (acc_gl_journal_entry.office_id=${branch} or ${branch}=1) "
                // +
                " group by name " + "order by glcode) credits " + "on debits.glcode=credits.glcode;";
        return sql;
    }

    private void setupBalanceMap(final String sql) {
        if (this.accountBalanceMap == null) {
            this.accountBalanceMap = new HashMap<>();
            final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);
            while (rs.next()) {
                this.accountBalanceMap.put(rs.getString("glcode"), rs.getBigDecimal("balance"));
            }
        }
    }

    // Calculate Taxonomy value from expression
    private BigDecimal processMappingString(String mappingString) {
        final ArrayList<String> glCodes = getGLCodes(mappingString);
        for (final String glcode : glCodes) {

            final BigDecimal balance = this.accountBalanceMap.get(glcode);
            mappingString = mappingString.replaceAll("\\{" + glcode + "\\}", balance != null ? balance.toString() : "0");
        }

        // evaluate the expression
        Float eval = 0f;
        try {
            final Number value = (Number) SCRIPT_ENGINE.eval(mappingString);
            if (value != null) {
                eval = value.floatValue();
            }
        } catch (final ScriptException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }

        return new BigDecimal(eval);
    }

    public ArrayList<String> getGLCodes(final String template) {

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