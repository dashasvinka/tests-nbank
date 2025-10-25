package api.requests.steps;

import api.models.AccountModel;
import api.models.GetProfileInfoResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import io.restassured.response.ValidatableResponse;
import api.models.*;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class AccountSteps {
    public static AccountModel findAccountById(GetProfileInfoResponse getProfileInfoResponse, Long id) {
        return getProfileInfoResponse.getAccounts().stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account with id " + id + " not found"));
    }

    public static Long createAccountAndGetId(String username, String password) {
        ValidatableResponse response = new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated()
        ).post(null);

        return response.extract().response().jsonPath().getLong("id");
    }
}
