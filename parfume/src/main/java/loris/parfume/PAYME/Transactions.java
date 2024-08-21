package loris.parfume.PAYME;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import loris.parfume.PAYME.Result.GetStatementResult;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transactions {

    private List<GetStatementResult> transactions;
}