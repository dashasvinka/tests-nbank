package api.models;

import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDepositRequest extends BaseModel{

    @GeneratingRule(regex = "^[1-9][0-9]{0,3}$")
    private long id;

    @GeneratingRule(regex = "^(?:[0-9]{1,3}|[1-4][0-9]{3}|5000)$")
    private double balance;
}
