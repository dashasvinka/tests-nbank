package requests.steps;

import models.CreateDepositRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class DepositSteps {
    public static void createDeposit(String username, String password, CreateDepositRequest request) {
        new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(request);
    }
}
