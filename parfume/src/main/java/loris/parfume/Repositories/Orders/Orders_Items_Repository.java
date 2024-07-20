package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Models.Orders.Orders_Items_Ids;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Orders_Items_Repository extends JpaRepository<Orders_Items, Orders_Items_Ids> {

    List<Orders_Items> findAllByOrder(Orders order);
}