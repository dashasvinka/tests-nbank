package requests.steps;

import models.GetProfileInfoResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class ProfileInfoSteps {
    public static GetProfileInfoResponse getProfile(String username, String password) {
        return new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE_INFO,
                ResponseSpecs.requestReturnsOK()
        ).get(GetProfileInfoResponse.class);
    }
}
