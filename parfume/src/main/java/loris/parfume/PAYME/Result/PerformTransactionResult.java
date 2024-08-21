package loris.parfume.PAYME.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PerformTransactionResult {

    private String transaction;
    private Long perform_time;
    private Integer state;
}