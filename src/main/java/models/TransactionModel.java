package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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
