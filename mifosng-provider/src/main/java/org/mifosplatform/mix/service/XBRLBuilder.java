/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mifosplatform.mix.data.ContextData;
import org.mifosplatform.mix.data.MixTaxonomyData;
import org.mifosplatform.mix.data.NamespaceData;
import org.mifosplatform.mix.data.XBRLData;
import org.mifosplatform.mix.exception.XBRLMappingInvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class XBRLBuilder {

    private static final String SCHEME_URL = "http://www.themix.org";
    private static final String IDENTIFIER = "000000";
    private static final String UNITID_PURE = "Unit1";
    private static final String UNITID_CUR = "Unit2";

    private Element root;
    private HashMap<ContextData, String> contextMap;
    Date startDate;
    Date endDate;
    private Integer instantScenarioCounter = 1;
    private Integer durationScenarioCounter = 1;

    @Autowired
    private NamespaceReadPlatformService readNamespaceService;

    public String build(final XBRLData xbrlData) {
        return this.build(xbrlData.getResultMap(), xbrlData.getStartDate(), xbrlData.getEndDate(), xbrlData.getCurrency());
    }

    public String build(final Map<MixTaxonomyData, BigDecimal> map, final Date startDate, final Date endDate, final String currency) {
        this.instantScenarioCounter = 1;
        this.durationScenarioCounter = 1;
        this.contextMap = new HashMap<>();
        final Document doc = DocumentHelper.createDocument();
        this.root = doc.addElement("xbrl");

        this.root.addElement("schemaRef").addNamespace("link",
                "http://www.themix.org/sites/default/files/Taxonomy2010/dct/dc-all_2010-08-31.xsd");

        this.startDate = startDate;
        this.endDate = endDate;

        for (final Entry<MixTaxonomyData, BigDecimal> entry : map.entrySet()) {
            final MixTaxonomyData taxonomy = entry.getKey();
            final BigDecimal value = entry.getValue();
            addTaxonomy(this.root, taxonomy, value);

        }

        addContexts();
        addCurrencyUnit(currency);
        addNumberUnit();

        doc.setXMLEncoding("UTF-8");

        return doc.asXML();
    }

    Element addTaxonomy(final Element rootElement, final MixTaxonomyData taxonomy, final BigDecimal value) {

        // throw an error is start / endate is null
        if (this.startDate == null || this.endDate == null) { throw new XBRLMappingInvalidException(
                "start date and end date should not be null"); }

        final String prefix = taxonomy.getNamespace();
        String qname = taxonomy.getName();
        if (prefix != null && (!prefix.isEmpty())) {
            final NamespaceData ns = this.readNamespaceService.retrieveNamespaceByPrefix(prefix);
            if (ns != null) {

                this.root.addNamespace(prefix, ns.url());
            }
            qname = prefix + ":" + taxonomy.getName();

        }
        final Element xmlElement = rootElement.addElement(qname);

        final String dimension = taxonomy.getDimension();
        final SimpleDateFormat timeFormat = new SimpleDateFormat("MM_dd_yyyy");

        ContextData context = null;
        if (dimension != null) {
            final String[] dims = dimension.split(":");

            if (dims.length == 2) {
                context = new ContextData(dims[0], dims[1], taxonomy.getType());
                if (this.contextMap.containsKey(context)) {

                } else {

                }
            }
        }

        if (context == null) {
            context = new ContextData(null, null, taxonomy.getType());
        }

        if (!this.contextMap.containsKey(context)) {

            final String startDateStr = timeFormat.format(this.startDate);
            final String endDateStr = timeFormat.format(this.endDate);

            final String contextRefID = (context.getPeriodType() == 0) ? ("As_Of_" + endDateStr + (this.instantScenarioCounter++))
                    : ("Duration_" + startDateStr + "_To_" + endDateStr + (this.durationScenarioCounter++));

            this.contextMap.put(context, contextRefID);
        }

        xmlElement.addAttribute("contextRef", this.contextMap.get(context));
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
    void addNumberUnit() {
        final Element numerUnit = this.root.addElement("unit");
        numerUnit.addAttribute("id", UNITID_PURE);
        final Element measure = numerUnit.addElement("measure");
        measure.addText("xbrli:pure");

    }

    /**
     * Adds the currency unit to the document
     * 
     * @param currencyCode
     */
    public void addCurrencyUnit(final String currencyCode) {
        final Element currencyUnitElement = this.root.addElement("unit");
        currencyUnitElement.addAttribute("id", UNITID_CUR);
        final Element measure = currencyUnitElement.addElement("measure");
        measure.addText("iso4217:" + currencyCode);

    }

    public void addContexts() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (final Entry<ContextData, String> entry : this.contextMap.entrySet()) {
            final ContextData context = entry.getKey();
            final Element contextElement = this.root.addElement("context");
            contextElement.addAttribute("id", entry.getValue());
            contextElement.addElement("entity").addElement("identifier").addAttribute("scheme", SCHEME_URL).addText(IDENTIFIER);

            final Element periodElement = contextElement.addElement("period");

            if (context.getPeriodType() == 0) {
                periodElement.addElement("instant").addText(format.format(this.endDate));
            } else {
                periodElement.addElement("startDate").addText(format.format(this.startDate));
                periodElement.addElement("endDate").addText(format.format(this.endDate));
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
