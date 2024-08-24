package loris.parfume.PAYME;

import loris.parfume.Models.Orders.Orders;
import loris.parfume.PAYME.Result.OrderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<OrderTransaction, Long> {

    OrderTransaction findByPaycomId(String id);
    OrderTransaction findByOrder(Orders order);

    List<OrderTransaction> findByCreateTimeBetween(Long createTime, Long createTime2);
}