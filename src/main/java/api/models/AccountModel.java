package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountModel extends BaseModel{
    private Long id;
    private String accountNumber;
    private Double balance;
    private List<TransactionModel> transactions;
}
