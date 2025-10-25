package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateTransferRequest  extends BaseModel {
    private long senderAccountId;
    private long receiverAccountId;
    private double amount;
}
