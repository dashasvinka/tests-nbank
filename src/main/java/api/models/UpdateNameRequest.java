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
public class UpdateNameRequest extends BaseModel {

    @GeneratingRule(regex = "^[A-Z][a-z]{1,9} [A-Z][a-z]{1,9}$")
    private String name;
}
