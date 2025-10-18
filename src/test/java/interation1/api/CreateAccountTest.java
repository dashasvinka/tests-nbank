package interation1.api;

import api.models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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
