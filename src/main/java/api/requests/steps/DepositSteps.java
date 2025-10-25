package api.requests.steps;

import api.models.CreateDepositRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class DepositSteps {
    public static void createDeposit(String username, String password, CreateDepositRequest request) {
        new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.DEPOSITS,
                ResponseSpecs.requestReturnsOK()
        ).post(request);
    }
}
