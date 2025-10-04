package requests.steps;

import generators.RandomModelGenerator;
import io.restassured.response.ValidatableResponse;
import models.*;
import requests.CreateAccountRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

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
