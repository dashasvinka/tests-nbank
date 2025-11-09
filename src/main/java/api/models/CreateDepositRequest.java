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

    @GeneratingRule(regex = "^(?:[1-9][0-9]|[1-9][0-9]{2}|[1-3][0-9]{3}|49[0-8][0-9]|4990)$")
    private double balance;
}
