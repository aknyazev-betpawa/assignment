package tests;

import config.Log4jTestWatcher;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


import static io.restassured.RestAssured.given;

import static java.util.concurrent.TimeUnit.SECONDS;


import static tests.TestUtil.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PayoutTests extends MockService {

    @Rule
    public TestWatcher testWatcher = new Log4jTestWatcher();

    CountDownLatch latch;

    static Logger log = Logger.getLogger(PayoutTests.class.getName());

    @Before
    public void init() {
        latch = new CountDownLatch(1);
        mockRemoteService();
        log.info("Callback server port: " + targetServer.port());
        log.info("Base test server port: " + rule.port());

    }


    @Test
    public void createRequest_checkRequestResponseCode_expect200() {
        given().
                body(payoutRequestCreationAcceptedFinalCompleted).
                when().
                post("/pawaPayBusiness/v1/payouts").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void createRequest_checkRequestResponseStatus_expectAccepted() {
        String status = given().
                body(payoutRequestCreationAcceptedFinalCompleted).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("ACCEPTED", status);
    }

    @Test
    public void createRequest_checkRequestResponseStatus_expectRejected() {
        String status = given().
                body(payoutRequestCreationRejected).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("REJECTED", status);
    }

    @Test
    public void createRequest_checkRequestResponseReason_expectRejectionReason() {
        String rejectionReason = given().
                body(payoutRequestCreationRejected).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("rejectionReason.rejectionReason");

        Assert.assertEquals("PAYOUTS_NOT_ALLOWED", rejectionReason);
    }

    @Test
    public void createRequest_checkRequestResponseStatus_expectDuplicateIgnored() {
        payoutRequestCreationDuplicateIgnored.setPayoutId("3");
        String status = given().
                body(payoutRequestCreationDuplicateIgnored).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("DUPLICATE_IGNORED", status);
    }

    @Test
    public void createRequest_checkRequestResponse_expectUnknownInternalError() {
        String internalError = given().
                body(payoutRequestCreationUnknownError).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("errorMessage");

        Assert.assertEquals("Unknown Internal Error", internalError);
    }


    @Test
    public void createRequest_payoutID2_checkCallbackHeader_expectHeader() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedFinalCompleted).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withHeader("Content-Type", equalTo("application/json"))
        );

    }

    @Test
    public void createRequest_payoutID2_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedFinalCompleted).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T17:33:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T17:33:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T17:33:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void createRequest_payoutID5_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedFinalCancelled).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T17:45:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T17:45:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T17:45:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void createRequest_payoutID6_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedFinalFailed).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T17:55:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T17:55:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T17:55:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void createRequest_payoutID7_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedPending).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T17:57:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T17:57:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T17:57:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void createRequest_payoutID8_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestCreationAcceptedSubmitted).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T17:59:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T17:59:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T17:59:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void createRequest_payoutID9_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutUnknownError).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2000-03-09T18:00:29Z\"," +
                        "\"amount\": \"15.21\"," +
                        "\"currency\": \"ZMW\"," +
                        "\"recipient\": {" +
                        "\"type\": \"MSISDN\"," +
                        "\"address\": {" +
                        "\"value\": 256780334452}}," +
                        "\"correspondent\": \"MTN_MOMO_ZMB\"," +
                        "\"country\": \"ZMB\"," +
                        "\"payoutId\": \"f4401bd2-1568-4140-bf2d-eb77d2b2b639\"," +
                        "\"statementDescription\": \"Payout #123\"," +
                        "\"customerTimestamp\": \"2000-03-09T18:00:29Z\"," +
                        "\"receivedByRecipient\": \"2000-03-09T18:00:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }


    @Test
    public void requestTransactionProcessingStatusCode_checkStatusCode_expect200() {
        given().
                when().
                get("/pawaPayBusiness/v1/payouts/2").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponseStatus_expectCompleted() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/2").
                jsonPath().
                get("status");

        Assert.assertEquals("COMPLETED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponseStatus_expectCancelled() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/5").
                jsonPath().
                get("[0].status");

        Assert.assertEquals("CANCELLED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponseStatus_expectFailed() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/6").
                jsonPath().
                get("[0].status");

        Assert.assertEquals("FAILED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponseStatus_expectPending() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/7").
                jsonPath().
                get("status");

        Assert.assertEquals("PENDING", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponseStatus_expectSubmitted() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/8").
                jsonPath().
                get("status");

        Assert.assertEquals("SUBMITTED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectUnknownError() {
        String errorMessage = given().
                when().
                get("/pawaPayBusiness/v1/payouts/9").
                jsonPath().
                get("errorMessage");

        Assert.assertEquals("Unknown Internal Error", errorMessage);
    }


}