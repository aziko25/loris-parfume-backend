package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Models.Orders.Orders_Items_Ids;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Orders_Items_Repository extends JpaRepository<Orders_Items, Orders_Items_Ids> {

    List<Orders_Items> findAllByOrder(Orders order);

    void deleteAllByItem(Items item);

    List<Orders_Items> findAllByCollection(Collections collection);

    List<Orders_Items> findAllBySize(Sizes size);

    void deleteAllByOrder(Orders order);
}