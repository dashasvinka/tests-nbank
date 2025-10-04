package interation1;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        // создание аккаунта
       new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
        Endpoint.ACCOUNTS,
        ResponseSpecs.entityWasCreated())
               .post(null);
    }
}
