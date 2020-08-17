package tests;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.wiremock.webhooks.Webhooks;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static org.wiremock.webhooks.Webhooks.webhook;

public class MockService {

    @Rule
    public  WireMockRule targetServer = new WireMockRule(8090);


    @Rule
    public  WireMockRule rule = new WireMockRule(
            options()
                    .port(8080)
                    .extensions(new Webhooks()));

     public  void mockRemoteService() {
        targetServer.stubFor(any(anyUrl())
                .willReturn(aResponse().withStatus(200)));

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '2')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID2RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T17:33:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T17:33:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '5')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID5RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T17:45:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T17:45:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '6')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID6RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T17:55:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T17:55:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '7')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID7RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T17:57:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T17:57:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '8')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID8RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T17:59:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T17:59:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '9')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutID9RequestCreationResponseAccepted.json"))
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
                                        "\"customerTimestamp\": \"2000-03-09T18:00:29Z\"," +
                                        "\"receivedByRecipient\": \"2000-03-09T18:00:30Z\"," +
                                        "\"correspondentIds\": {" +
                                        "\"MTN_INIT\": \"ABC123\"," +
                                        "\"MTN_FINAL\": \"DEF456\"}," +
                                        "\"status\": \"ACCEPTED\"" +
                                        "}}"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '1')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestCreationResponseRejected.json"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '3')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestCreationResponseDuplicateIgnored.json"))
        );

        rule.stubFor(post(urlPathEqualTo("/pawaPayBusiness/v1/payouts"))
                .withRequestBody(matchingJsonPath(
                        "$.[?(@.payoutId== '4')]"))
                .willReturn(aResponse().withStatus(200).withBodyFile("json/payoutRequestCreationUnknownError.json"))
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
