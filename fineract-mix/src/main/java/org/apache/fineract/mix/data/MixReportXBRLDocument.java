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
package org.apache.fineract.mix.data;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlRootElement(name = "xbrl")
@XmlType(propOrder = { "schemaRef", "taxonomyElements", "contexts", "units" })
public class MixReportXBRLDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<JAXBElement<TaxonomyValue>> taxonomyElements = new ArrayList<>();
    private List<XBRLContextDTO> contexts = new ArrayList<>();
    private List<XBRLUnitDTO> units = new ArrayList<>();

    @XmlElement(name = "schemaRef")
    public SchemaRefDTO getSchemaRef() {
        return new SchemaRefDTO();
    }

    @XmlAnyElement(lax = true)
    public List<JAXBElement<TaxonomyValue>> getTaxonomyElements() {
        return this.taxonomyElements;
    }

    @XmlElement(name = "context")
    public List<XBRLContextDTO> getContexts() {
        return this.contexts;
    }

    @XmlElement(name = "unit")
    public List<XBRLUnitDTO> getUnits() {
        return this.units;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TaxonomyValue implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @XmlAttribute(name = "contextRef")
        private String contextRef;

        @XmlAttribute(name = "unitRef")
        private String unitRef;

        @XmlAttribute(name = "decimals")
        private String decimals;

        @XmlValue
        private BigDecimal value;
    }

    public static class SchemaRefDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        public String getNamespaceUri() {
            return "http://www.themix.org/sites/default/files/Taxonomy2010/dct/dc-all_2010-08-31.xsd";
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class XBRLContextDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String id;
        private EntityDTO entity = new EntityDTO();
        private PeriodDTO period;
        private ScenarioDTO scenario;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class EntityDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private IdentifierDTO identifier = new IdentifierDTO();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class IdentifierDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String scheme = "http://www.themix.org";
        private String value = "000000";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PeriodDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String instant;
        private String startDate;
        private String endDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ScenarioDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private ExplicitMemberDTO explicitMember;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExplicitMemberDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String dimension;
        private String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class XBRLUnitDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String id;
        private String measure;
    }
}
