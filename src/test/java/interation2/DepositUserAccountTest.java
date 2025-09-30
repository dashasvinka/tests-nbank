package interation2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class DepositUserAccountTest {
    @Test
    public void userCanMakeDepositIntoOwnAccount() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwate20001qQ",
                          "password": "Wwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwate20001qQ",
                          "password": "Wwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");
        String requestBody = """
            {
              "id": %s,
              "balance": 3000
            }
            """.formatted(id);

        // Create deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountOver5000() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwate20001qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwate20001qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");
        String requestBody = """
            {
              "id": %s,
              "balance": 5000.01
            }
            """.formatted(id);

        // Create deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                // Deposit amount cannot exceed 5000
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountNegativeSum() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwate20002qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwate20002qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");
        String requestBody = """
            {
              "id": %s,
              "balance": -1
            }
            """.formatted(id);

        // Create deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                // Deposit amount must be at least 0.01
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanNotMakeDepositIntoOwnAccountSumEqualToZero() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwate20003qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwate20003qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");
        String requestBody = """
            {
              "id": %s,
              "balance": 0
            }
            """.formatted(id);

        // Create deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                // Deposit amount must be at least 0.01
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanNotMakeDepositIntoUnCreatedAccount() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwate20008qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwate20008qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");
        String requestBody = """
            {
              "id": 303039330,
              "balance": 100
            }
            """;

        // Create deposit
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                // Unauthorized access to account
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void userCanNotMakeDepositIntoAnotherUsersAccountt() {
        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwate40003qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwate40003qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response response = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String id = response.jsonPath().getString("id");

        // Create user
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "Wwwwa1te30003qQ",
                          "password": "Wwwwate1qQ$",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // Get auth token
        String userAnotherAuthHeader =
                given()
                        .contentType(ContentType.JSON)
                        .accept(ContentType.JSON)
                        .body("""
                        {
                          "username": "Wwwwa1te30003qQ",
                          "password": "Wwwwate1qQ$"
                        }
                        """)
                        .post("http://localhost:4111/api/v1/auth/login")
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .header("Authorization");
        // Create account
        Response responseNew = RestAssured
                .given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract().response();
        String idNew = response.jsonPath().getString("id");

        String requestBody = """
            {
              "id": %s,
              "balance": 0
            }
            """.formatted(id);

        // Create deposit
        given()
                .header("Authorization", userAnotherAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                // Deposit amount must be at least 0.01
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }
}
