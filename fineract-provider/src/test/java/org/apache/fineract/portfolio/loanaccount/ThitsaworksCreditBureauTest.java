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
package org.apache.fineract.portfolio.loanaccount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.File;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.creditbureau.service.CreditReportWritePlatformServiceImpl;
import org.apache.fineract.infrastructure.creditbureau.service.ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ThitsaworksCreditBureauTest {

    private FromJsonHelper fromApiJsonHelper = new FromJsonHelper();

    @Mock
    private ThitsaWorksCreditBureauIntegrationWritePlatformServiceImpl thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl;

    @InjectMocks
    private final CreditReportWritePlatformServiceImpl creditReportWritePlatformServiceImpl = null;

    /*
     * @Autowired CreditBureauIntegrationTest(final FromJsonHelper fromApiJsonHelper) { this.fromApiJsonHelper =
     * fromApiJsonHelper; }
     */

    static String process = "process";
    static String nrcID = "13/MiFoS(N)163525";

    static String userName = "demomfi1@mifos.com";
    static String password = "password";
    static String subscriptionKey = "15c15ff17493acb44cb223f2feab2fe4";

    static String subscriptionId = "4A7C-317A41BA-1FF8-8A64-F8EDBE0F625D";
    static String url = "url";
    static String token = "token";
    static Long uniqueId = 8113399260L;
    static byte[] file;
    static File report;
    static String fileName;
    static UriInfo uriInfo;
    static FormDataContentDisposition fileData;

    static String testresult = "{   'Data': {'BorrowerInfo': {'MainIdentifier': '2113439293', 'Name': 'Aung Khant Min',"
            + "            'NRC': '13/MiFoS(N)163525', 'Gender': '', 'DOB': '1990-01-20', 'FatherName': '', 'Address': '',"
            + "            'LastUpdatedDtm': 'Jul  8 2020  9:27AM', 'PrintedDtm': 'Aug  2 2020  2:54AM'  },  "
            + "     'ActiveLoanStatus': 'Record found',  'ActiveLoans': [{'ReportingDate': '2020-03-01',"
            + "                'LoanGUID': 'BC309AD3-0444-4EFD-807E-79ECB99EE999',"
            + "                'Institution': 'Demo 1',   'Division': 'YANGON ',"
            + "                'Township': 'YANKIN ', 'DisbursedDate': 'xxxx',"
            + "                'DisbursedAmount': '500000', 'PrincipalOutstandingAmount': '300000',"
            + "                'TotalOutstandingAmount': 'xxxx', 'PrincipalOverdueAmount': '100000',"
            + "                'TotalOverdueAmount': '', 'DaysInDelay': '10',"
            + "                'UpdatedDtm': 'Apr 27 2020  9:41AM', 'SortColumn': '2020-03-01T00:00:00'}], "
            + "     'WriteOffLoanStatus': 'No record found', 'WriteOffLoans': null,"
            + "     'CreditScore': { 'Score': 'N/A','Class': 'N/A',  'Note': ''},"
            + "    'InquiryStatus': 'Record found', 'Inquiries': [{ 'InquiryDate': '', 'Institution': '','NoOfInquiry': ''} ] },"
            + "    'MessageDtm': '8/1/2020 8:24:20 PM UTC','SubscriptionID': '4A7C-317A41BA-1FF8-8A64-F8EDBE0F625D',"
            + "    'CallerIP': '207.46.228.155',"
            + "    'URI': 'https://qa-mmcix-api.azurewebsites.net/20200324/api/Dashboard/GetCreditReport?uniqueId=2113439292',"
            + "    'ResponseMessage': 'Record found'}";

    @Test
    public void getUniqueIdFromSearchMethodTest() {
        String searchResult = "{\"Data\":[{\"UniqueID\":\"8113399260\",\"NRC\":\"12/KaMaRa(N)253426\",\"FullName\":\"Aye Aye\",\"DOB\":\"1990-05-22,1991-05-22\",\"FatherFullName\":\"U Aye Myint Maung\",\"Location\":\"Yangon-Thongwa,Twantay,Yankin\",\"Flag\":\"[{\\\"WriteOff\\\":1}]\",\"Active\":\"Y\"}],\"MessageDtm\":\"8/1/2020 6:39:00 PM UTC\",\"SubscriptionID\":\"317A1FF8-625D-41BA-BE0F-F8ED8A644A7C\",\"CallerIP\":\"207.46.228.155\",\"URI\":\"https://qa-mmcix-api.azurewebsites.net/20200324/api/Search/SimpleSearch?nrc=253426\",\"ResponseMessage\":\"Record found\"}";

        when(this.thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.okHttpConnectionMethod(userName, password, subscriptionKey,
                subscriptionId, url, token, report, fileData, uniqueId, nrcID, process)).thenReturn(searchResult);

        final String search = thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.okHttpConnectionMethod(userName, password,
                subscriptionKey, subscriptionId, url, token, report, fileData, uniqueId, nrcID, process);

        when(thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.extractUniqueId(search)).thenCallRealMethod();

        Long uniqueId = thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.extractUniqueId(searchResult);

        Long expectedUniqueId = 8113399260L;
        assertEquals(expectedUniqueId, uniqueId);

    }

    @Test
    public void getCreditReportTest() {
        String curentNrc = "13/MiFoS(N)163525";

        when(this.thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.okHttpConnectionMethod(userName, password, subscriptionKey,
                subscriptionId, url, token, report, fileData, uniqueId, nrcID, process)).thenReturn(testresult);

        String creditReport = thitsaWorksCreditBureauIntegrationWritePlatformServiceImpl.okHttpConnectionMethod(userName, password,
                subscriptionKey, subscriptionId, url, token, report, fileData, uniqueId, nrcID, process);

        JsonObject resultObject = JsonParser.parseString(creditReport).getAsJsonObject();
        String data = resultObject.get("Data").toString();

        JsonObject dataObject = JsonParser.parseString(data).getAsJsonObject();
        String borrowerInfo = dataObject.get("BorrowerInfo").toString();

        JsonObject borrowerObject = JsonParser.parseString(borrowerInfo).getAsJsonObject();
        String nrc = borrowerObject.get("NRC").toString();

        nrc = nrc.substring(1, nrc.length() - 1);

        assertEquals(nrc, curentNrc);

    }

}
