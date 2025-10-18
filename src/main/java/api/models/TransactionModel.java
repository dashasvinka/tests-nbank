package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionModel extends BaseModel {
        private Long id;
        private Double amount;
        private String type;
        private String timestamp;
        private Long relatedAccountId;
}
