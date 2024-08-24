package loris.parfume.UZUM;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UzumOrderTransactionsRepository extends JpaRepository<UzumOrderTransactions, Long> {

    Optional<UzumOrderTransactions> findByTransId(String transId);
}