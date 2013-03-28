import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.path.json.JsonPath.from;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.mifosplatform.integrationtests.testbuilder.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.testbuilder.Utils;

/**
 * Client Loan Integration Test for checking Loan Disburstment with Waive Interest and Write-Off.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class LoanWithWaiveInterestAndWriteOff {

    ResponseSpecification responseSpec;
    RequestSpecification requestSpec;
    final String PRINCIPAL="4,500.00",
            NUMBER_OF_REPAYMENTS="9",
            REPAYMENT_PERIOD="2",
            DISBURSEMENT_DATE="30 October 2010",
            LOAN_APPLICATION_SUBMISSION_DATE="23 September 2010",
            INTEREST_VALUE_AMOUNT = "40.00";

    @Before
    public void setup() {
        String basicAuthKey = loginIntoServerAndGetBase64EncodedAuthenticationKey();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization","Basic "+basicAuthKey);

        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow(){
        //CREATE CLIENT
        Integer clientID = createClient();
        verifyClientCreatedOnServer(clientID);

        //CREATE LOAN PRODUCT
        Integer loanProductID = createLoanProduct();
        //APPLY FOR LOAN
        Integer loanID = applyForLoanApplication(clientID, loanProductID);
        HashMap loanStatusHashMap = getStatusOfLoan(loanID);
        verifyLoanStatusIsPending(loanStatusHashMap);

        //APPROVE LOAN
        loanStatusHashMap = approveLoan("28 September 2010", loanID);
        System.out.println("********************HASHMAP **************"+loanStatusHashMap);
        verifyLoanIsApproved(loanStatusHashMap);
        verifyLoanStatusIsWaitingForDisbursal(loanStatusHashMap);

        //UNDO APPROVAL
        loanStatusHashMap = undoApproval(loanID);
        verifyLoanNeedsApproval(loanStatusHashMap);

        //RE-APPROVE LOAN ON 1 OCTOBER 2010
        loanStatusHashMap = approveLoan("1 October 2010", loanID);
        verifyLoanIsApproved(loanStatusHashMap);
        verifyLoanStatusIsWaitingForDisbursal(loanStatusHashMap);

        //DISBURSE
        loanStatusHashMap = disburseLoan(loanID);
        verifyLoanStatusIsActive(loanStatusHashMap);

        //PERFORM REPAYMENTS AND CHECK LOAN STATUS
        //-----------------------------------------
//        verifyRepaymentScheduleEntryFor(1, 4000.0F, 200.0F, loanID);

        makeRepayment(540.0f, "1 January 2011", loanID);
        makeRepayment(540.0f, "1 March 2011", loanID);
        waiveInterest("1 May 2011", loanID);
        makeRepayment(500.0f, "1 May 2011", loanID);
        makeRepayment(540.0f, "1 July 2011", loanID);
        waiveInterest("1 September 2011", loanID);
        makeRepayment(500.0f, "1 September 2011", loanID);
        makeRepayment(540.0f, "1 November 2011", loanID);
        waiveInterest("1 January 2012", loanID);
        makeRepayment(500.0f, "1 January 2012", loanID);

//        verifyRepaymentScheduleEntryFor(7, 1000.0F, 200.0F, loanID);

        //WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        verifyLoanAccountIsClosed(writeOffLoan("1 March 2012", loanID));

    }

    private void verifyLoanNeedsApproval(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    private void verifyLoanIsApproved(HashMap loanStatusHashMap) {
        assertFalse(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    private void verifyLoanStatusIsWaitingForDisbursal(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "waitingForDisbursal"));
    }

    private void verifyLoanStatusIsPending(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "pendingApproval"));
    }

    private void verifyLoanStatusIsActive(HashMap loanStatusHashMap) {
        assertTrue(getStatus(loanStatusHashMap, "active"));
    }

    private void verifyRepaymentScheduleEntryFor(int repaymentNumber, float expectedPrincipal, Float expectedInterest, Integer loanID) {
        ArrayList<HashMap> repaymentPeriods = getLoanRepaymentSchedule(loanID);
        assertEquals(repaymentPeriods.get(repaymentNumber).get("principalLoanBalanceOutstanding"), expectedPrincipal);
        assertEquals(repaymentPeriods.get(repaymentNumber).get("interestLoanBalanceOutstanding"), expectedInterest);
    }

    private HashMap writeOffLoan(String transactionDate, Integer loanID){
        System.out.println("--------------------LOAN WRITTEN OFF ON "+transactionDate+"-------------------------------\n");
        String json = given().spec(requestSpec).body(getWriteOffBodyAsJSON(transactionDate))
                .expect().spec(responseSpec).log().ifError()
                .when().post("/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=writeoff&tenantIdentifier=default")
                .andReturn().asString();
        HashMap response = from(json).get("changes");
        return (HashMap)response.get("status");
    }

    private void waiveInterest(String transactionDate, Integer loanID){
        System.out.println("--------------------Waive INTEREST On "+transactionDate+"-------------------------------\n");
        given().spec(requestSpec).body(getWaiveBodyAsJSON(transactionDate))
                      .expect().spec(responseSpec).log().ifError()
                      .when().post("/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=waiveinterest&tenantIdentifier=default");
    }

    private void makeRepayment(Float transactionAmount, String transactionDate, Integer loanID){
        System.out.print("------------------------MADE REPAYMENT ON "+transactionDate+"---------------------------------\n");
        given().spec(requestSpec).body(getRepaymentBodyAsJSON(transactionDate,transactionAmount))
        .expect().spec(responseSpec).log().ifError()
        .when().post("/mifosng-provider/api/v1/loans/" + loanID + "/transactions?command=repayment&tenantIdentifier=default");
    }

    private ArrayList  getLoanRepaymentSchedule(Integer loanID)
    {
        System.out.println("---------------------------GETTING LOAN REPAYMENT SCHEDULE--------------------------------");
        String json = given().spec(requestSpec)
                .expect().spec(responseSpec).log().ifError()
                .when().get("/mifosng-provider/api/v1/loans/" + loanID +"?associations=repaymentSchedule&tenantIdentifier=default").andReturn().asString();
        HashMap repaymentSchedule = from(json).get("repaymentSchedule");
       return (ArrayList)repaymentSchedule.get("periods");
    }

    private boolean getStatus(HashMap loanStatusMap,String nameOfLoanStatusString){
       return (Boolean)loanStatusMap.get(nameOfLoanStatusString);
    }

    private void verifyLoanAccountIsClosed(HashMap loanStatusHashMap){
        Assert.assertTrue(getStatus(loanStatusHashMap, "closed"));
    }

    private HashMap getStatusOfLoan(Integer loanID){
        String json= given().spec(requestSpec)
                     .expect().spec(responseSpec).log().ifError()
                     .when().get("/mifosng-provider/api/v1/loans/" + loanID + "?tenantIdentifier=default")
                     .andReturn().asString();
        return from(json).get("status");
    }

    private HashMap undoApproval(Integer loanID){
        System.out.println("-----------------------------------UNDO LOAN APPROVAL-----------------------------------------");
        String json =  given().spec(requestSpec).body("{'note':'UNDO APPROVAL'}")
                    .expect().spec(responseSpec).log().ifError()
                    .when().post("/mifosng-provider/api/v1/loans/" + loanID + "?command=undoApproval&tenantIdentifier=default")
                    .andReturn().asString();
        HashMap response = from(json).get("changes");
        return (HashMap)response.get("status");
    }
    private HashMap disburseLoan(Integer loanID){
        System.out.println("-----------------------------------DISBURSE LOAN-----------------------------------------");
        String json =  given().spec(requestSpec).body(getDisburseLoanAsJSON())
                    .expect().spec(responseSpec).log().ifError()
                    .when().post("/mifosng-provider/api/v1/loans/" + loanID + "?command=disburse&tenantIdentifier=default")
                    .andReturn().asString();
        HashMap response = from(json).get("changes");
        return (HashMap)response.get("status");
    }

    private HashMap approveLoan(String approvalDate, Integer loanID){
        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        String json =given().spec(requestSpec).body(getApproveLoanAsJSON(approvalDate))
                     .expect().spec(responseSpec).log().ifError()
                     .when().post("/mifosng-provider/api/v1/loans/"+ loanID +"?command=approve&tenantIdentifier=default")
                     .andReturn().asString();
        System.out.println("**************CHANGES  :"+from(json).get("changes"));
        HashMap response = from(json).get("changes");
        return (HashMap)response.get("status");
    }


    private String loginIntoServerAndGetBase64EncodedAuthenticationKey() {
        System.out.println("-----------------------------------LOGIN-----------------------------------------");
        String json = post("/mifosng-provider/api/v1/authentication?username=mifos&password=password&tenantIdentifier=default").asString();
        return JsonPath.with(json).get("base64EncodedAuthenticationKey");
    }

    private Integer createClient() {
        System.out.println("---------------------------------CREATING A CLIENT---------------------------------------------");
        String json = given().spec(requestSpec).body(getTestClientAsJSON())
                .expect().spec(responseSpec).log().ifError()
                .when().post("/mifosng-provider/api/v1/clients?tenantIdentifier=default")
                .andReturn().asString();

        return  from(json).get("clientId");
    }

    private void verifyClientCreatedOnServer(final Integer generatedClientID) {
        System.out.println("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        given().spec(requestSpec)
                .expect().spec(responseSpec).log().ifError()
                .when().get("/mifosng-provider/api/v1/clients/" + generatedClientID + "?tenantIdentifier=default");
    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String loanProductJSON = new LoanProductTestBuilder()
                .withPrincipal(PRINCIPAL)
                .withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(REPAYMENT_PERIOD)
                .withNumberOfRepayments(NUMBER_OF_REPAYMENTS)
                .build();
        String json = given().spec(requestSpec).body(loanProductJSON)
                .expect().spec(responseSpec).log().ifError()
                .when().post("/mifosng-provider/api/v1/loanproducts?tenantIdentifier=default")
                .andReturn().asString();
        return from(json).get("resourceId");
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        String json = given().spec(requestSpec).body(getLoanApplicationBodyAsJSON(clientID.toString(), loanProductID.toString()))
                        .expect().spec(responseSpec).log().ifError()
                        .when().post("/mifosng-provider/api/v1/loans?tenantIdentifier=default")
                        .andReturn().asString();
        return from(json).get("loanId");
    }


    private String getWriteOffBodyAsJSON(String transactionDate){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("dateFormat","dd MMMM yyyy");
        map.put("locale","en");
        map.put("note", " LOAN WRITE OFF!!!");
        map.put("transactionDate",transactionDate);
        return new Gson().toJson(map);
    }
    private String getWaiveBodyAsJSON(String transactionDate){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("transactionDate",transactionDate);
        map.put("transactionAmount",INTEREST_VALUE_AMOUNT);
        map.put("note", " Interest Waived!!!");
        return new Gson().toJson(map);
    }
    private String getRepaymentBodyAsJSON(String transactionDate,Float transactionAmount){
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("locale","en");
        map.put("dateFormat","dd MMMM yyyy");
        map.put("transactionDate",transactionDate);
        map.put("transactionAmount",transactionAmount.toString());
        map.put("note", "Repayment Made!!!");
        return new Gson().toJson(map);
    }
    private String getApproveLoanAsJSON(String approvalDate){
      HashMap<String,String> map = new HashMap<String, String>();
      map.put("locale","en");
      map.put("dateFormat","dd MMMM yyyy");
      map.put("approvedOnDate",approvalDate);
      map.put("note", "Approval NOTE");
      System.out.println("***************JSON ***"+new Gson().toJson(map));
      return new Gson().toJson(map);
    }

    private String getDisburseLoanAsJSON(){
      HashMap<String,String> map = new HashMap<String, String>();
      map.put("locale","en");
      map.put("dateFormat","dd MMMM yyyy");
      map.put("actualDisbursementDate",DISBURSEMENT_DATE);
      map.put("note", "DISBURSE NOTE");
      return new Gson().toJson(map);
    }

    private String getTestClientAsJSON() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("officeId", "1");
        map.put("firstname", Utils.randomNameGenerator("Client_FirstName_", 5));
        map.put("lastname", Utils.randomNameGenerator("Client_LastName_", 4));
        map.put("externalId", Utils.randomIDGenerator("ID_", 7));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("joinedDate", "04 March 2009");
        System.out.println("map : "+map);
        return new Gson().toJson(map);
    }


    private String getLoanApplicationBodyAsJSON(final String clientID,final String productID){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("clientId", clientID);
        map.put("productId", productID);
        map.put("principal", PRINCIPAL);
        map.put("loanTermFrequency", "4");
        map.put("loanTermFrequencyType", "2");
        map.put("numberOfRepayments", NUMBER_OF_REPAYMENTS);
        map.put("repaymentEvery", REPAYMENT_PERIOD);
        map.put("repaymentFrequencyType", "2");
        map.put("interestRateFrequencyType", "2");
        map.put("interestRatePerPeriod", "2");
        map.put("amortizationType", "1");
        map.put("interestType", "1");
        map.put("interestCalculationPeriodType", "1");
        map.put("transactionProcessingStrategyId", "1");
        map.put("expectedDisbursementDate", DISBURSEMENT_DATE);
        map.put("submittedOnDate", LOAN_APPLICATION_SUBMISSION_DATE);
        return new Gson().toJson(map);
    }

    private String getLoanCalculationBodyAsJSON(String productID){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("productId", productID);
        map.put("principal", PRINCIPAL);
        map.put("loanTermFrequency", "4");
        map.put("loanTermFrequencyType", "2");
        map.put("numberOfRepayments", NUMBER_OF_REPAYMENTS);
        map.put("repaymentEvery", REPAYMENT_PERIOD);
        map.put("repaymentFrequencyType", "2");
        map.put("interestRateFrequencyType", "2");
        map.put("interestRatePerPeriod", "2");
        map.put("amortizationType", "1");
        map.put("interestType", "1");
        map.put("interestCalculationPeriodType", "1");
        map.put("expectedDisbursementDate", DISBURSEMENT_DATE);
        map.put("transactionProcessingStrategyId", "1");
        return new Gson().toJson(map);
    }


}






















































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































































