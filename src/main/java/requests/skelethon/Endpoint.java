package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

@Getter
@AllArgsConstructor
public enum Endpoint {

    ADMIN_USER("/admin/users", CreateUserRequest.class, CreateUserResponse.class),
    ACCOUNTS("/accounts", BaseModel.class, CreateAccountResponse.class),
    LOGIN("/auth/login", LoginUserRequest.class, LoginUserResponse.class),

    PROFILE_INFO("/customer/profile",GetProfileInfoResponse.class, GetProfileInfoResponse.class),

    UPDATE_NAME("/customer/profile", UpdateNameRequest.class, BaseModel.class),

    DEPOSITS("/accounts/deposit", CreateDepositRequest.class, BaseModel.class),

    TRANSFER("/accounts/transfer", CreateTransferRequest.class, BaseModel.class);
    private final String url;
    private final Class<? extends BaseModel> requestModel;

    private final Class<? extends BaseModel> responseModel;
}
