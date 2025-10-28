package api.requests.steps;

import api.models.CreateAccountResponse;
import api.models.GetProfileInfoResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.requests.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import ui.utils.RetryUtils;
import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<CreateAccountResponse> getAllAccountsWithEmptyValidation() {
        ValidatedCrudRequester<CreateAccountResponse> requester =
                new ValidatedCrudRequester<>(
                        RequestSpecs.authAsUser(username, password),
                        Endpoint.CUSTOMER_ACCOUNTS,
                        ResponseSpecs.requestReturnsOK()
                );
        return RetryUtils.retry(
                () -> requester.getAll(CreateAccountResponse[].class),
                accounts -> accounts != null && !accounts.isEmpty(),
                10,
                2000
        );
    }

    public static GetProfileInfoResponse getProfileInfo(String username, String password){
        return  new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
    }
}
