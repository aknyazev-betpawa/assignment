package tests;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import config.Log4jTestWatcher;
import dataentities.FinancialAddress;
import dataentities.Payout;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;
import org.wiremock.webhooks.Webhooks;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.concurrent.CountDownLatch;

import static com.flipkart.zjsonpatch.JsonDiff.asJson;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;

import static io.restassured.RestAssured.given;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.wiremock.webhooks.Webhooks.webhook;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PayoutTests {


    @Rule
    public TestWatcher testWatcher = new Log4jTestWatcher();

    @Rule
    public WireMockRule targetServer = new WireMockRule(8090);
    CountDownLatch latch;

    @Rule
    public WireMockRule rule = new WireMockRule(
            options()
                    .port(8080)
                    .extensions(new Webhooks()));


    FinancialAddress financialAddress = new FinancialAddress("MSISDN", "256780334452");
    Payout payouRequestRejected = new Payout("1", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 30));
    Payout payoutRequestAcceptedCallBackAccepted = new Payout("2", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 33));
    Payout payoutRequestDuplicateIgnored = new Payout("2", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 33));
    Payout payoutRequestUnknownError = new Payout("4", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 40));
    Payout payoutRequestAcceptedCallBackAcceptedFinalCancelled = new Payout("5", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 45));
    Payout payoutRequestAcceptedCallBackAcceptedFinalFailed = new Payout("6", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 55));
    Payout payoutRequestAcceptedCallBackAcceptedPending = new Payout("7", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 57));
    Payout payoutRequestAcceptedCallBackAcceptedSubmitted = new Payout("8", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 59));
    Payout payoutUnknownError = new Payout("9", "15.21", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 18, 00));

    static Logger log = Logger.getLogger(PayoutTests.class.getName());

    @Before
    public void init() {
        latch = new CountDownLatch(1);
        log.info("Target server port: " + targetServer.port());
        log.info("Under test server port: " + rule.port());
        mockRemoteService();

    }


    @Test
    public void sendRequest_checkResponseCode_expect200() {
        given().
                body(payoutRequestAcceptedCallBackAccepted).
                when().
                post("/pawaPayBusiness/v1/payouts").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void sendRequest_checkRequestResponseBody_expectAccepted() {
        String status = given().
                body(payoutRequestAcceptedCallBackAccepted).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("ACCEPTED", status);
    }

    @Test
    public void sendRequest_checkRequestResponseStatus_expectRejected() {
        String status = given().
                body(payouRequestRejected).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("REJECTED", status);
    }

    @Test
    public void sendRequest_checkRequestResponseReason_expectRejectionReason() {
        String rejectionReason = given().
                body(payouRequestRejected).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("rejectionReason.rejectionReason");

        Assert.assertEquals("PAYOUTS_NOT_ALLOWED", rejectionReason);
    }

    @Test
    public void sendRequest_checkRequestResponseBody_expectDuplicateIgnored() {
        payoutRequestDuplicateIgnored.setPayoutId("3");
        String status = given().
                body(payoutRequestDuplicateIgnored).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("DUPLICATE_IGNORED", status);
    }

    @Test
    public void sendRequest_checkRequestResponse_expectUnknownInternalError() {
        String internalError = given().
                body(payoutRequestUnknownError).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("errorMessage");

        Assert.assertEquals("Unknown Internal Error", internalError);
    }


    @Test
    public void sendRequest_checkCallbackHeader_expectHeader() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAccepted).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withHeader("Content-Type", equalTo("application/json"))
        );

    }

    @Test
    public void sendRequestID2_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAccepted).
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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void sendRequestID5_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAcceptedFinalCancelled).
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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void sendRequestID6_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAcceptedFinalFailed).
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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void sendRequestID7_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAcceptedPending).
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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void sendRequestID8_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payoutRequestAcceptedCallBackAcceptedSubmitted).
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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }

    @Test
    public void sendRequestID9_checkCallbackRequestBody_expectRequestBody() throws Exception {

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
                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                        "\"correspondentIds\": {" +
                        "\"MTN_INIT\": \"ABC123\"," +
                        "\"MTN_FINAL\": \"DEF456\"}," +
                        "\"status\": \"ACCEPTED\"" +
                        "}}"))
        );
    }



    @Test
    public void requestTransactionProcessingStatusCode_checkResponseCode_expect200() {
        given().
                when().
                get("/pawaPayBusiness/v1/payouts/2").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectCompleted() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/2").
                jsonPath().
                get("status");

        Assert.assertEquals("COMPLETED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectCancelled() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/5").
                jsonPath().
                get("[0].status");

        Assert.assertEquals("CANCELLED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectFailed() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/6").
                jsonPath().
                get("[0].status");

        Assert.assertEquals("FAILED", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectPending() {
        String status = given().
                when().
                get("/pawaPayBusiness/v1/payouts/7").
                jsonPath().
                get("status");

        Assert.assertEquals("PENDING", status);
    }

    @Test
    public void requestTransactionProcessingResponse_checkResponse_expectSubmitted() {
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





    private void mockRemoteService() {

        targetServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '2')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T17:33:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '5')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T17:45:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '6')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T17:55:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );
        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '7')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T17:57:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '8')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T17:59:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '9')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseAcceptedId2.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2000-03-09T18:00:29Z\"," +
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
                                        "\"customerTimestamp\": \"2020-02-21T17:32:28Z\"," +
                                        "\"receivedByRecipient\": \"2020-02-21T17:32:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '1')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseRejected.json"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '3')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestResponseDuplicateIgnored.json"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '4')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestUnknownError.json"))
        );

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutFinalCompleted.json")));

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/5"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutFinalCancelled.json")));

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/6"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutFinalFailed.json")));

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/7"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutPending.json")));

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutSubmitted.json")));

        rule.stubFor(get(urlEqualTo("/pawaPayBusiness/v1/payouts/9"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/payoutUnknownError.json")));



    }



}