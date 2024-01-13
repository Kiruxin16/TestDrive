package api.pojo;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegResponse {
    @JsonSetter("register_data")
    private RegisterData registerData;
    private Info info;

}
