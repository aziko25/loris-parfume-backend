package loris.parfume.PAYME.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelTransactionResult {

    private String transaction;
    private Long cancel_time;
    private Integer state;
}