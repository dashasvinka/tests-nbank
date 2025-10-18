package interation1;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
       CreateUserRequest userRequest = AdminSteps.createUser();

        // создание аккаунта
       new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
        Endpoint.ACCOUNTS,
        ResponseSpecs.entityWasCreated())
               .post(null);
    }
}
