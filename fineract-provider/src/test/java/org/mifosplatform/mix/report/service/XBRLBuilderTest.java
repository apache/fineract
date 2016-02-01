/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.report.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifosplatform.mix.data.MixTaxonomyData;
import org.mifosplatform.mix.data.NamespaceData;
import org.mifosplatform.mix.service.NamespaceReadPlatformServiceImpl;
import org.mifosplatform.mix.service.XBRLBuilder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class XBRLBuilderTest {

    @Mock
    private NamespaceReadPlatformServiceImpl readNamespaceService;
    @InjectMocks
    private final XBRLBuilder xbrlBuilder = new XBRLBuilder();

    @Before
    public void setUp() throws Exception {

        this.readNamespaceService = Mockito.mock(NamespaceReadPlatformServiceImpl.class);
        when(this.readNamespaceService.retrieveNamespaceByPrefix(Matchers.anyString())).thenReturn(
                new NamespaceData(1l, "mockedprefix", "mockedurl"));

    }

    @SuppressWarnings("null")
    @Test
    public void shouldCorrectlyBuildMap() {

        final HashMap<MixTaxonomyData, BigDecimal> map = new HashMap<MixTaxonomyData, BigDecimal>();
        final MixTaxonomyData data1 = Mockito.mock(MixTaxonomyData.class);
        when(data1.getName()).thenReturn("Assets");
        map.put(data1, new BigDecimal(10000));
        final String result = this.xbrlBuilder.build(map, Date.valueOf("2005-11-11"), Date.valueOf("2013-07-17"), "USD");
        System.out.println(result);
        NodeList nodes = null;
        try {
            nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(result.getBytes()))
                    .getElementsByTagName("Assets");
        } catch (final SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(nodes);
        assertNotNull(nodes.item(0));
        assertEquals("Assets", nodes.item(0).getNodeName());
    }

}
