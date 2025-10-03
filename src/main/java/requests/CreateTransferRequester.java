package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CreateTransferRequest;

import static io.restassured.RestAssured.given;

public class CreateTransferRequester  extends Request<CreateTransferRequest>{
    public CreateTransferRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(CreateTransferRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .post("/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(CreateTransferRequest model) {
        throw new UnsupportedOperationException("PUT is not supported in UpdateUserNameRequester");
    }

    @Override
    public ValidatableResponse get() {
        throw new UnsupportedOperationException("GET is not supported in UpdateUserNameRequester");
    }
}
