package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNameRequest extends BaseModel {

    @GeneratingRule(regex = "^[A-Z][a-z]+ [A-Z][a-z]+$")
    private String name;
}
