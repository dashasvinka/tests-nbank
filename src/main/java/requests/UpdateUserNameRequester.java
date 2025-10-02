package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.UpdateNameRequest;

import static io.restassured.RestAssured.given;

public class UpdateUserNameRequester extends Request<UpdateNameRequest> {
    public UpdateUserNameRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse put(UpdateNameRequest model) {
        return given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse post(UpdateNameRequest model) {
        throw new UnsupportedOperationException("POST is not supported in UpdateUserNameRequester");
    }
}
