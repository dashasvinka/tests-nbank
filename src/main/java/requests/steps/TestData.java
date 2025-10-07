package requests.steps;

import models.CreateDepositRequest;
import models.CreateTransferRequest;
import models.UpdateNameRequest;

public class TestData {
    public static UpdateNameRequest buildUpdateNameRequest(String name) {
        return UpdateNameRequest.builder()
                .name(name)
                .build();
    }

    public static CreateDepositRequest buildCreateDepositRequest(Long id, int balance) {
        return CreateDepositRequest.builder()
                .id(id)
                .balance(balance)
                .build();
    }

    public static CreateTransferRequest buildCreateTransferRequest(Long senderId, Long receiverId, double amount) {
        return CreateTransferRequest.builder()
                .senderAccountId(senderId)
                .receiverAccountId(receiverId)
                .amount(amount)
                .build();
    }
}
