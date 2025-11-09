package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import common.helpers.StepLogger;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {

    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        return StepLogger.log("Admin creates user " + userRequest.getUsername(), () -> {
                    new ValidatedCrudRequester<CreateUserResponse>(
                            RequestSpecs.adminSpec(),
                            Endpoint.ADMIN_USER,
                            ResponseSpecs.entityWasCreated())
                            .post(userRequest);

                    return userRequest;
                }
        );
    }

    public static List<CreateUserResponse> getAllUsers() {
        return StepLogger.log("Admin gets all users", () -> {
            return new ValidatedCrudRequester<CreateUserResponse>(
                    RequestSpecs.adminSpec(),
                    Endpoint.ADMIN_USER,
                    ResponseSpecs.requestReturnsOK()).getAll(CreateUserResponse[].class);
        });
    }
}
