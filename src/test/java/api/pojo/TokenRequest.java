package api.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequest {
    private String password;
    private String username;

    public TokenRequest(RegRequest request){
        this.password= request.getPass();
        this.username=request.getLogin();
    }
}
