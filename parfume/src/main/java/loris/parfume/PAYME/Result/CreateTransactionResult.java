package loris.parfume.PAYME.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionResult {

    private long create_time;
    private String transaction;
    private Integer state;
}