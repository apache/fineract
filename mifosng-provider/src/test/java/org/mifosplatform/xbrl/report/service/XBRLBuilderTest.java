package org.mifosplatform.xbrl.report.service;

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
import org.mifosplatform.xbrl.report.data.NamespaceData;
import org.mifosplatform.xbrl.taxonomy.data.TaxonomyData;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class XBRLBuilderTest {

    @Mock
    private ReadNamespaceServiceImpl readNamespaceService;
    @InjectMocks
    private XBRLBuilder xbrlBuilder = new XBRLBuilder();

    @Before
    public void setUp() throws Exception {

        readNamespaceService = Mockito.mock(ReadNamespaceServiceImpl.class);
        when(readNamespaceService.retrieveNamespaceByPrefix(Mockito.anyString())).thenReturn(
                new NamespaceData(1l, "mockedprefix", "mockedurl"));

    }

    @Test
    public void shouldCorrectlyBuildMap() {

        HashMap<TaxonomyData, BigDecimal> map = new HashMap<TaxonomyData, BigDecimal>();
        TaxonomyData data1 = Mockito.mock(TaxonomyData.class);
        when(data1.getName()).thenReturn("Assets");
        map.put(data1, new BigDecimal(10000));
        String result = xbrlBuilder.build(map, Date.valueOf("2005-11-11"), Date.valueOf("2013-07-17"), "USD");
        System.out.println(result);
        NodeList nodes = null;
        try {
            nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(result.getBytes()))
                    .getElementsByTagName("Assets");
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(nodes);
        assertNotNull(nodes.item(0));
        assertEquals("Assets", nodes.item(0).getNodeName());
    }

}
