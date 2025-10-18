package api.requests.steps;

import api.models.GetProfileInfoResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requests.CrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class ProfileInfoSteps {
    public static GetProfileInfoResponse getProfile(String username, String password) {
        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
    }
}
