package api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetProfileInfoResponse  extends BaseModel{
        private Long id;
        private String username;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @ToString.Exclude
        private String password;

        private String name;
        private String role;
        private List<AccountModel> accounts;
}
