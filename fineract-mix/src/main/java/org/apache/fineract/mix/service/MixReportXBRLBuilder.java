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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.mix.data.MixReportXBRLContextData;
import org.apache.fineract.mix.data.MixReportXBRLData;
import org.apache.fineract.mix.data.MixReportXBRLDocument;
import org.apache.fineract.mix.data.MixReportXBRLNamespaceData;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.apache.fineract.mix.exception.MixReportXBRLMappingInvalidException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MixReportXBRLBuilder {

    private static final String SCHEME_URL = "http://www.themix.org";
    private static final String IDENTIFIER = "000000";
    private static final String UNITID_PURE = "Unit1";
    private static final String UNITID_CUR = "Unit2";

    private final MixReportXBRLNamespaceReadService readNamespaceService;

    public String build(final MixReportXBRLData xbrlData) {
        return this.build(xbrlData.getResultMap(), xbrlData.getStartDate(), xbrlData.getEndDate(), xbrlData.getCurrency());
    }

    public String build(final Map<MixTaxonomyData, BigDecimal> map, final Date startDate, final Date endDate, final String currency) {
        MixReportXBRLData xbrlData = new MixReportXBRLData(map, startDate, endDate, currency);
        MixReportXBRLDocument doc = buildDocumentGraph(xbrlData);
        try {
            JAXBContext context = JAXBContext.newInstance(MixReportXBRLDocument.class, MixReportXBRLDocument.TaxonomyValue.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(doc, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Error marshalling XBRL document", e);
            throw new RuntimeException(e);
        }
    }

    public MixReportXBRLDocument buildDocumentGraph(final MixReportXBRLData xbrlData) {
        MixReportXBRLDocument document = new MixReportXBRLDocument();

        Integer instantScenarioCounter = 0;
        Integer durationScenarioCounter = 0;
        Map<MixReportXBRLContextData, String> contextMap = new HashMap<>();

        // 1. Build the dynamic taxonomy elements using JAXBElement
        for (final Map.Entry<MixTaxonomyData, BigDecimal> entry : xbrlData.getResultMap().entrySet()) {
            final MixTaxonomyData taxonomy = entry.getKey();
            final BigDecimal value = entry.getValue();

            if (xbrlData.getStartDate() == null || xbrlData.getEndDate() == null) {
                throw new MixReportXBRLMappingInvalidException("start date and end date should not be null");
            }

            final String prefix = taxonomy.getNamespace();
            String localName = taxonomy.getName();
            String nsUrl = "";
            if (prefix != null && !prefix.isEmpty()) {
                final MixReportXBRLNamespaceData ns = this.readNamespaceService.retrieveNamespaceByPrefix(prefix);
                if (ns != null) {
                    nsUrl = ns.getUrl();
                }
            }

            MixReportXBRLContextData context = getContextForTaxonomy(taxonomy);

            if (!contextMap.containsKey(context)) {
                final SimpleDateFormat timeFormat = new SimpleDateFormat("MM_dd_yyyy");
                final String startDateStr = timeFormat.format(xbrlData.getStartDate());
                final String endDateStr = timeFormat.format(xbrlData.getEndDate());
                instantScenarioCounter += 1;
                durationScenarioCounter += 1;
                final String contextRefID = context.getPeriodType() == 0 ? "As_Of_" + endDateStr + instantScenarioCounter
                        : "Duration_" + startDateStr + "_To_" + endDateStr + durationScenarioCounter;

                contextMap.put(context, contextRefID);
            }

            MixReportXBRLDocument.TaxonomyValue taxValue = new MixReportXBRLDocument.TaxonomyValue(contextMap.get(context),
                    getUnitRef(taxonomy), getNumberOfDecimalPlaces(value).toString(), value);

            QName qname = new QName(nsUrl, localName, prefix != null ? prefix : "");
            jakarta.xml.bind.JAXBElement<MixReportXBRLDocument.TaxonomyValue> jaxbElement = new jakarta.xml.bind.JAXBElement<>(qname,
                    MixReportXBRLDocument.TaxonomyValue.class, taxValue);

            document.getTaxonomyElements().add(jaxbElement);
        }

        // 2. Map Contexts into the structural DTO layout
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        for (final Map.Entry<MixReportXBRLContextData, String> entry : contextMap.entrySet()) {
            final MixReportXBRLContextData context = entry.getKey();
            MixReportXBRLDocument.XBRLContextDTO contextDTO = new MixReportXBRLDocument.XBRLContextDTO();
            contextDTO.setId(entry.getValue());

            MixReportXBRLDocument.PeriodDTO periodDTO = new MixReportXBRLDocument.PeriodDTO();
            if (context.getPeriodType() == 0) {
                periodDTO.setInstant(format.format(xbrlData.getEndDate()));
            } else {
                periodDTO.setStartDate(format.format(xbrlData.getStartDate()));
                periodDTO.setEndDate(format.format(xbrlData.getEndDate()));
            }
            contextDTO.setPeriod(periodDTO);

            final String dimension = context.getDimension();
            final String dimType = context.getDimensionType();
            if (dimType != null && dimension != null) {
                MixReportXBRLDocument.ScenarioDTO scenarioDTO = new MixReportXBRLDocument.ScenarioDTO();
                MixReportXBRLDocument.ExplicitMemberDTO memberDTO = new MixReportXBRLDocument.ExplicitMemberDTO();
                memberDTO.setDimension(dimType);
                memberDTO.setValue(dimension);
                scenarioDTO.setExplicitMember(memberDTO);
                contextDTO.setScenario(scenarioDTO);
            }

            document.getContexts().add(contextDTO);
        }

        // 3. Map Units into structured DTO layout
        MixReportXBRLDocument.XBRLUnitDTO pureUnit = new MixReportXBRLDocument.XBRLUnitDTO();
        pureUnit.setId(UNITID_PURE);
        pureUnit.setMeasure("xbrli:pure");
        document.getUnits().add(pureUnit);

        MixReportXBRLDocument.XBRLUnitDTO curUnit = new MixReportXBRLDocument.XBRLUnitDTO();
        curUnit.setId(UNITID_CUR);
        curUnit.setMeasure("iso4217:" + xbrlData.getCurrency());
        document.getUnits().add(curUnit);

        return document;
    }

    private MixReportXBRLContextData getContextForTaxonomy(final MixTaxonomyData taxonomy) {
        MixReportXBRLContextData context = null;
        final String dimension = taxonomy.getDimension();
        if (dimension != null) {
            final List<String> dims = Splitter.on(':').splitToList(dimension);

            if (dims.size() == 2) {
                context = new MixReportXBRLContextData().setDimensionType(dims.get(0)).setDimension(dims.get(1)).setPeriodType(
                        taxonomy.getType().equals(MixTaxonomyData.BALANCE_SHEET) || taxonomy.getType().equals(MixTaxonomyData.PORTFOLIO) ? 0
                                : 1);
            }
        }

        if (context == null) {
            context = new MixReportXBRLContextData().setPeriodType(
                    taxonomy.getType().equals(MixTaxonomyData.BALANCE_SHEET) || taxonomy.getType().equals(MixTaxonomyData.PORTFOLIO) ? 0
                            : 1);
        }
        return context;
    }

    private String getUnitRef(final MixTaxonomyData tx) {
        return tx.isPortfolio() ? UNITID_PURE : UNITID_CUR;
    }

    private Integer getNumberOfDecimalPlaces(final BigDecimal bigDecimal) {
        final String string = bigDecimal.stripTrailingZeros().toPlainString();
        final int index = string.indexOf(".");
        return index < 0 ? 0 : string.length() - index - 1;
    }
}
