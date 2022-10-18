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

import com.google.common.base.Splitter;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.mix.data.ContextData;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.data.NamespaceData;
import org.apache.fineract.mix.data.XBRLData;
import org.apache.fineract.mix.exception.XBRLMappingInvalidException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XBRLBuilder {

    private static final String SCHEME_URL = "http://www.themix.org";
    private static final String IDENTIFIER = "000000";
    private static final String UNITID_PURE = "Unit1";
    private static final String UNITID_CUR = "Unit2";

    @Autowired
    private NamespaceReadPlatformService readNamespaceService;

    public String build(final XBRLData xbrlData) {
        return this.build(xbrlData.getResultMap(), xbrlData.getStartDate(), xbrlData.getEndDate(), xbrlData.getCurrency());
    }

    public String build(final Map<MixTaxonomyData, BigDecimal> map, final Date startDate, final Date endDate, final String currency) {
        Integer instantScenarioCounter = 0;
        Integer durationScenarioCounter = 0;
        Map<ContextData, String> contextMap = new HashMap<>();
        final Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("xbrl");

        root.addElement("schemaRef").addNamespace("link",
                "http://www.themix.org/sites/default/files/Taxonomy2010/dct/dc-all_2010-08-31.xsd");

        for (final Map.Entry<MixTaxonomyData, BigDecimal> entry : map.entrySet()) {
            final MixTaxonomyData taxonomy = entry.getKey();
            final BigDecimal value = entry.getValue();
            addTaxonomy(root, taxonomy, value, startDate, endDate, instantScenarioCounter, durationScenarioCounter, contextMap);

        }

        addContexts(root, startDate, endDate, contextMap);
        addCurrencyUnit(root, currency);
        addNumberUnit(root);

        doc.setXMLEncoding("UTF-8");

        return doc.asXML();
    }

    private Element addTaxonomy(final Element rootElement, final MixTaxonomyData taxonomy, final BigDecimal value, final Date startDate,
            final Date endDate, Integer instantScenarioCounter, Integer durationScenarioCounter,
            final Map<ContextData, String> contextMap) {

        // throw an error is start / endate is null
        if (startDate == null || endDate == null) {
            throw new XBRLMappingInvalidException("start date and end date should not be null");
        }

        final String prefix = taxonomy.getNamespace();
        String qname = taxonomy.getName();
        if (prefix != null && !prefix.isEmpty()) {
            final NamespaceData ns = this.readNamespaceService.retrieveNamespaceByPrefix(prefix);
            if (ns != null) {

                rootElement.addNamespace(prefix, ns.getUrl());
            }
            qname = prefix + ":" + taxonomy.getName();

        }
        final Element xmlElement = rootElement.addElement(qname);

        final String dimension = taxonomy.getDimension();
        final SimpleDateFormat timeFormat = new SimpleDateFormat("MM_dd_yyyy");

        ContextData context = null;
        if (dimension != null) {
            final List<String> dims = Splitter.on(':').splitToList(dimension);

            if (dims.size() == 2) {
                context = new ContextData().setDimensionType(dims.get(0)).setDimension(dims.get(1)).setPeriodType(
                        taxonomy.getType().equals(MixTaxonomyData.BALANCESHEET) || taxonomy.getType().equals(MixTaxonomyData.PORTFOLIO) ? 0
                                : 1);
            }
        }

        if (context == null) {
            context = new ContextData().setPeriodType(
                    taxonomy.getType().equals(MixTaxonomyData.BALANCESHEET) || taxonomy.getType().equals(MixTaxonomyData.PORTFOLIO) ? 0
                            : 1);
        }

        if (!contextMap.containsKey(context)) {

            final String startDateStr = timeFormat.format(startDate);
            final String endDateStr = timeFormat.format(endDate);
            instantScenarioCounter += 1;
            durationScenarioCounter += 1;
            final String contextRefID = context.getPeriodType() == 0 ? "As_Of_" + endDateStr + instantScenarioCounter
                    : "Duration_" + startDateStr + "_To_" + endDateStr + durationScenarioCounter;

            contextMap.put(context, contextRefID);
        }

        xmlElement.addAttribute("contextRef", contextMap.get(context));
        xmlElement.addAttribute("unitRef", getUnitRef(taxonomy));
        xmlElement.addAttribute("decimals", getNumberOfDecimalPlaces(value).toString());

        // add the child
        xmlElement.addText(value.toPlainString());

        return xmlElement;
    }

    private String getUnitRef(final MixTaxonomyData tx) {
        return tx.isPortfolio() ? UNITID_PURE : UNITID_CUR;
    }

    /**
     * Adds the generic number unit
     */
    private void addNumberUnit(final Element root) {
        final Element numerUnit = root.addElement("unit");
        numerUnit.addAttribute("id", UNITID_PURE);
        final Element measure = numerUnit.addElement("measure");
        measure.addText("xbrli:pure");

    }

    /**
     * Adds the currency unit to the document
     *
     * @param currencyCode
     */
    private void addCurrencyUnit(final Element root, final String currencyCode) {
        final Element currencyUnitElement = root.addElement("unit");
        currencyUnitElement.addAttribute("id", UNITID_CUR);
        final Element measure = currencyUnitElement.addElement("measure");
        measure.addText("iso4217:" + currencyCode);

    }

    private void addContexts(final Element root, final Date startDate, final Date endDate, final Map<ContextData, String> contextMap) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (final Map.Entry<ContextData, String> entry : contextMap.entrySet()) {
            final ContextData context = entry.getKey();
            final Element contextElement = root.addElement("context");
            contextElement.addAttribute("id", entry.getValue());
            contextElement.addElement("entity").addElement("identifier").addAttribute("scheme", SCHEME_URL).addText(IDENTIFIER);

            final Element periodElement = contextElement.addElement("period");

            if (context.getPeriodType() == 0) {
                periodElement.addElement("instant").addText(format.format(endDate));
            } else {
                periodElement.addElement("startDate").addText(format.format(startDate));
                periodElement.addElement("endDate").addText(format.format(endDate));
            }

            final String dimension = context.getDimension();
            final String dimType = context.getDimensionType();
            if (dimType != null && dimension != null) {
                contextElement.addElement("scenario").addElement("explicitMember").addAttribute("dimension", dimType).addText(dimension);
            }
        }

    }

    private Integer getNumberOfDecimalPlaces(final BigDecimal bigDecimal) {
        final String string = bigDecimal.stripTrailingZeros().toPlainString();
        final int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}
