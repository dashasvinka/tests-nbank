package interation2;

import generators.RandomData;
import models.CreateUserRequest;
import models.UpdateNameRequest;
import models.UserRole;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.UpdateUserNameRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UpdateUserNameTest {

    @Test
    public void userCanUpdateNameWithCorrectData() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name("Ivan Petrov")
                .build();
        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(updateNameRequest);
    }

    @Test
    public void userCanNotUpdateNameToOneWordName() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name("Vasia")
                .build();

        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Name must contain two words with letters only"))
                .put(updateNameRequest);
    }

    @Test
    public void userCanNotUpdateNameToTwoWordNameWithSeparatedInvalidChar() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name("Vasia_Vasiliev")
                .build();

        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Name must contain two words with letters only"))
                .put(updateNameRequest);
    }

    @Test
    public void userCanNotUpdateNameToNameWithSpecialSymbolsOrNumbers() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        // создание пользователя
        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateNameRequest updateNameRequest = UpdateNameRequest.builder()
                .name("Vasia1 Vasiliev$")
                .build();

        // изменение имени пользователя
        new UpdateUserNameRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequest("Name must contain two words with letters only"))
                .put(updateNameRequest);
    }
}
