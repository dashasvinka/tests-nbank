package api.requests.skelethon.requests;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;
    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        return (T) crudRequester.post(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public T put(BaseModel model) {
        return (T) crudRequester.put(model).extract().as(endpoint.getResponseModel());
    }

    public T get() {
        return (T) crudRequester.get().extract().as(endpoint.getResponseModel());
    }

    @Override
    public Object get(int id) {
        throw new UnsupportedOperationException("get(int id) не используется");
    }


    @Override
    public Object update(long id, BaseModel model) {
        return null;
    }

    @Override
    public Object delete(long id) {
        return null;
    }
}
