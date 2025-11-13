package api.requests.skelethon.requests;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("PUT запрос на {endpoint} c телом {model}")
    public ValidatableResponse put(BaseModel model){
        var body = model == null ? "" : model;
        return given()
                .spec(requestSpecification)
                .body(body)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    // Новый метод get() с указанием класса модели
    @Step("GET запрос на {endpoint} c телом  ответа {responseClass}")
    public <R> R get(Class<R> responseClass) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract()
                .as(responseClass);
    }


    // Новый метод get без параметра
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    // Метод get с id для интерфейса
    @Override
    @Step("GET запрос на {endpoint} с id {id}")
    public Object get(int id) {
        throw new UnsupportedOperationException("get(int id) не используется");
    }


    @Override
    @Step("PUT запрос на {endpoint} с id {id} и телом {model}")
    public Object update(long id, BaseModel model){
        return null;
    }

    @Override
    @Step("DELETE запрос на {endpoint} с id {id}")
    public Object delete(long id){
        return null;
    }

    @Override
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
