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

    public String build(XBRLData xbrlData) {
        return this.build(xbrlData.getResultMap(), xbrlData.getStartDate(), xbrlData.getEndDate(), xbrlData.getCurrency());
    }

    public String build(Map<MixTaxonomyData, BigDecimal> map, Date startDate, Date endDate, String currency) {
        instantScenarioCounter = 1;
        durationScenarioCounter = 1;
        contextMap = new HashMap<ContextData, String>();
        Document doc = DocumentHelper.createDocument();
        root = doc.addElement("xbrl");

        root.addElement("schemaRef").addNamespace("link",
                "http://www.themix.org/sites/default/files/Taxonomy2010/dct/dc-all_2010-08-31.xsd");

        this.startDate = startDate;
        this.endDate = endDate;

        for (Entry<MixTaxonomyData, BigDecimal> entry : map.entrySet()) {
            MixTaxonomyData taxonomy = entry.getKey();
            BigDecimal value = entry.getValue();
            this.addTaxonomy(root, taxonomy, value);

        }

        this.addContexts();
        this.addCurrencyUnit(currency);
        this.addNumberUnit();

        doc.setXMLEncoding("UTF-8");

        return doc.asXML();
    }

    Element addTaxonomy(Element rootElement, MixTaxonomyData taxonomy, BigDecimal value) {

        // throw an error is start / endate is null
        if (startDate == null || endDate == null) { throw new XBRLMappingInvalidException("start date and end date should not be null"); }

        String prefix = taxonomy.getNamespace();
        String qname = taxonomy.getName();
        if (prefix != null && (!prefix.isEmpty())) {
            NamespaceData ns = readNamespaceService.retrieveNamespaceByPrefix(prefix);
            if (ns != null) {

                root.addNamespace(prefix, ns.url());
            }
            qname = prefix + ":" + taxonomy.getName();

        }
        Element xmlElement = rootElement.addElement(qname);

        String dimension = taxonomy.getDimension();
        SimpleDateFormat timeFormat = new SimpleDateFormat("MM_dd_yyyy");

        ContextData context = null;
        if (dimension != null) {
            String[] dims = dimension.split(":");

            if (dims.length == 2) {
                context = new ContextData(dims[0], dims[1], taxonomy.getType());
                if (contextMap.containsKey(context)) {

                } else {

                }
            }
        }

        if (context == null) {
            context = new ContextData(null, null, taxonomy.getType());
        }

        if (!contextMap.containsKey(context)) {

            String startDateStr = timeFormat.format(startDate);
            String endDateStr = timeFormat.format(endDate);

            String contextRefID = (context.getPeriodType() == 0) ? ("As_Of_" + endDateStr + (instantScenarioCounter++)) : ("Duration_"
                    + startDateStr + "_To_" + endDateStr + (durationScenarioCounter++));

            contextMap.put(context, contextRefID);
        }

        xmlElement.addAttribute("contextRef", contextMap.get(context));
        xmlElement.addAttribute("unitRef", getUnitRef(taxonomy));
        xmlElement.addAttribute("decimals", getNumberOfDecimalPlaces(value).toString());

        // add the child
        xmlElement.addText(value.toPlainString());

        return xmlElement;
    }

    private String getUnitRef(MixTaxonomyData tx) {
        return tx.isPortfolio() ? UNITID_PURE : UNITID_CUR;
    }

    /**
     * Adds the generic number unit
     */
    void addNumberUnit() {
        Element numerUnit = root.addElement("unit");
        numerUnit.addAttribute("id", UNITID_PURE);
        Element measure = numerUnit.addElement("measure");
        measure.addText("xbrli:pure");

    }

    /**
     * Adds the currency unit to the document
     * 
     * @param currencyCode
     */
    public void addCurrencyUnit(String currencyCode) {
        Element currencyUnitElement = root.addElement("unit");
        currencyUnitElement.addAttribute("id", UNITID_CUR);
        Element measure = currencyUnitElement.addElement("measure");
        measure.addText("iso4217:" + currencyCode);

    }

    public void addContexts() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (Entry<ContextData, String> entry : contextMap.entrySet()) {
            ContextData context = entry.getKey();
            Element contextElement = root.addElement("context");
            contextElement.addAttribute("id", entry.getValue());
            contextElement.addElement("entity").addElement("identifier").addAttribute("scheme", SCHEME_URL).addText(IDENTIFIER);

            Element periodElement = contextElement.addElement("period");

            if (context.getPeriodType() == 0) {
                periodElement.addElement("instant").addText(format.format(endDate));
            } else {
                periodElement.addElement("startDate").addText(format.format(startDate));
                periodElement.addElement("endDate").addText(format.format(endDate));
            }

            String dimension = context.getDimension();
            String dimType = context.getDimensionType();
            if (dimType != null && dimension != null) {
                contextElement.addElement("scenario").addElement("explicitMember").addAttribute("dimension", dimType).addText(dimension);
            }
        }

    }

    private Integer getNumberOfDecimalPlaces(BigDecimal bigDecimal) {
        String string = bigDecimal.stripTrailingZeros().toPlainString();
        int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}
