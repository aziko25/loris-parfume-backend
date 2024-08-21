package loris.parfume.PAYME;

import loris.parfume.Models.Orders.Orders;
import loris.parfume.PAYME.Result.OrderTransaction;
import loris.parfume.PAYME.Result.TransactionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<OrderTransaction, Long> {

    OrderTransaction findByPaycomId(String id);
    OrderTransaction findByOrder(Orders order);

    @Query("select o from OrderTransaction o " +
            "where o.paycomTime between ?1 and ?2 and o.state = ?3 ORDER BY o.paycomTime ASC")
    List<OrderTransaction> findByPaycomTimeAndState(Date from, Date to, TransactionState state);

    List<OrderTransaction> findByCreateTimeBetween(Long createTime, Long createTime2);
}