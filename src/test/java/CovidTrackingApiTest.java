import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.qameta.allure.*;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

@Epic("API Testing")
@Feature("COVID Tracking API Tests")
public class CovidTrackingApiTest {

    private static final String BASE_URL = "https://api.covidtracking.com/v1";
    private static final String STATES_ENDPOINT = "/states/current.json";

    @BeforeAll
    public static void setup() {
        // Set base URI for all requests
        baseURI = BASE_URL;
    }

    @Test
    @Story("GET States Data")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Test to verify that the states endpoint returns a valid response")
    @DisplayName("Test COVID Tracking API states endpoint returns 200 and valid data")
    public void testStatesEndpoint() {
        // Make the request and get the response
        Response response = given()
            .contentType(ContentType.JSON)
        .when()
            .get(STATES_ENDPOINT)
        .then()
            .statusCode(200)  // Verify status code is 200
            .contentType(ContentType.JSON)  // Verify content type is JSON
            .body("", instanceOf(java.util.List.class))  // Verify response is an array
            .extract()
            .response();
        
        // Additional validations on the response body
        response.then()
            .body("size()", greaterThan(0))  // Verify array is not empty
            .body("state", everyItem(notNullValue()))  // Verify every item has state field
            .body("positive", everyItem(isA(Number.class)))  // Verify positive cases are numbers
            .body("findAll{it.state=='NY'}", not(empty()))  // Verify New York is in the results
            .body("findAll{it.state=='CA'}", not(empty()));  // Verify California is in the results
    }

    @Test
    @Story("GET States Data Fields Validation")
    @Severity(SeverityLevel.NORMAL)
    @Description("Test to verify all required data fields are present in the response")
    @DisplayName("Test specific data fields in the response")
    public void testDataFields() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(STATES_ENDPOINT)
        .then()
            .statusCode(200)
            .body("every { it.containsKey('state') }", is(true))  // Every item has state field
            .body("every { it.containsKey('positive') }", is(true))  // Every item has positive field
            .body("every { it.containsKey('negative') }", is(true))  // Every item has negative field
            .body("every { it.containsKey('death') }", is(true))  // Every item has death field
            .body("every { it.containsKey('recovered') }", is(true))  // Every item has recovered field
            .body("every { it.state.length() == 2 }", is(true));  // Verify state codes are 2 letters
    }

    @Test
    @Story("Performance and Headers")
    @Severity(SeverityLevel.MINOR)
    @Description("Test response time and headers for the states endpoint")
    @DisplayName("Test response time and headers")
    public void testResponseTimeAndHeaders() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .get(STATES_ENDPOINT)
        .then()
            .statusCode(200)
            .time(lessThan(5000L))  // Response should be within 5 seconds
            .header("Content-Type", containsString("application/json"));  // Header validation
    }
}

