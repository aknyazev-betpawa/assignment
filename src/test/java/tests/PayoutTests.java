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
    Payout payout1 = new Payout("1", "1", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 55));
    Payout payout2 = new Payout("2", "1", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 55));
    Payout payout3 = new Payout("3", "1", "ZMW", financialAddress, "MTN_MOMO_ZMB", "ZMB",
            "Payout #123", LocalDateTime.of(2000, Month.MARCH, 9, 17, 55));

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
                body(payout2).
                when().
                post("/pawaPayBusiness/v1/payouts").
                then().
                assertThat().
                statusCode(200);
    }

    @Test
    public void sendRequest_checkRequestResponseBody_expectAccepted() {
        String status = given().
                body(payout2).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("ACCEPTED", status);
    }

    @Test
    public void sendRequest_checkRequestResponseBody_expectRejected() {
        String status = given().
                body(payout1).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("REJECTED", status);
    }

    @Test
    public void sendRequest_checkRequestResponseBody_expectDuplicateIgnored() {
        String status = given().
                body(payout3).
                when().
                post("/pawaPayBusiness/v1/payouts").
                jsonPath().
                get("status");

        Assert.assertEquals("DUPLICATE_IGNORED", status);
    }





    @Test
    public void sendRequest_checkCallbackHeader_expectHeader() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payout2).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withHeader("Content-Type", equalTo("application/json"))
        );

    }

    @Test
    public void sendRequest_checkCallbackRequestBody_expectRequestBody() throws Exception {

        verify(0, postRequestedFor(anyUrl()));

        given().
                body(payout2).
                when().
                post("/pawaPayBusiness/v1/payouts");
        latch.await(2, SECONDS);

        targetServer.verify(1, postRequestedFor(urlEqualTo("/callback"))
                .withRequestBody(equalToJson("{\"created\": \"2020-02-21T17:32:29Z\"," +
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
                        "\"status\": \"ACCEPTED\"," +
                        "\"failureReason\": {"+
                        "\"failureReason\": \"OTHER_ERROR\","+
                        "\"failureMessage\": \"Recipient's address is blocked\""+
                        "}}"))
        );
    }





  /*  @Test
    public void requestTransaction1_checkResponseCode_expect404() {
        given().
                when().
                get("/pawaPayBusiness/v1/payouts/1").
                then().
                assertThat().
                statusCode(404);
    }


    @Test
    public void requestTransaction2_checkResponseCode_expect200() {
        given().
                when().
                get("/pawaPayBusiness/v1/payouts/2").
                then().
                assertThat().
                statusCode(200);
    }
*/




    private void mockRemoteService() {

        targetServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '2')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutResponseAccepted.json"))
                .withPostServeAction("webhook", webhook()
                        .withMethod(POST)
                        .withUrl("http://localhost:" + targetServer.port() + "/callback")
                        .withHeader("Content-Type", "application/json").
                                withBody("{\"created\": \"2020-02-21T17:32:29Z\"," +
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
                                        "\"status\": \"ACCEPTED\"," +
                                        "\"failureReason\": {"+
                                       "\"failureReason\": \"OTHER_ERROR\","+
                                        "\"failureMessage\": \"Recipient's address is blocked\""+
                                        "}}"))
        );



        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '1')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutResponseRejected.json"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '3')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutResponseDuplicateIgnored.json"))
        );



    }

}