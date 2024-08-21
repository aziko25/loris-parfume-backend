package loris.parfume.PAYME.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckPerformTransactionResult {

    private boolean allow;

    private String code; // error code
    private String message; // error message
}
