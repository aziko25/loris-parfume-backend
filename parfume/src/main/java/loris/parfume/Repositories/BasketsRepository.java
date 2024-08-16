package loris.parfume.Repositories;

import loris.parfume.Models.Basket;
import loris.parfume.Models.Basket_Ids;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Users;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BasketsRepository extends JpaRepository<Basket, Basket_Ids> {

    List<Basket> findAllByUser(Users user, Sort addedTime);

    void deleteByUserAndItem(Users user, Items item);

    void deleteAllByUser(Users user);

    void deleteAllByItem(Items item);

    Basket findByUserAndItemAndSize(Users user, Items item, Sizes size);

    void deleteByUserAndItemAndSize(Users user, Items item, Sizes size);

    void deleteAllBySize(Sizes size);
}