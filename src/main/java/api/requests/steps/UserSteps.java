package api.requests.steps;

import api.models.AccountResponse;
import api.models.CreateAccountResponse;
import api.models.CreateUserResponse;
import api.models.GetProfileInfoResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

import static io.restassured.RestAssured.given;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccounts(){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);
    }

    public static GetProfileInfoResponse getProfileInfo(String username, String password){
        return  new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
    }
}
