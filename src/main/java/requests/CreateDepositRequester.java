package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateDepositRequest;

import static io.restassured.RestAssured.given;

public class CreateDepositRequester extends Request<CreateDepositRequest>{
    public CreateDepositRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateDepositRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(CreateDepositRequest model) {
        throw new UnsupportedOperationException("PUT is not supported in UpdateUserNameRequester");
    }
}
