package loris.parfume.Repositories;

import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Users;
import loris.parfume.Models.Wishlist;
import loris.parfume.Models.Wishlist_Ids;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Wishlist_Ids> {

    List<Wishlist> findAllByUser(Users user, Sort sort);

    void deleteByUserAndItem(Users user, Items item);

    void deleteAllByUser(Users user);

    void deleteAllByItem(Items item);

    void deleteByUserAndItemAndSize(Users user, Items item, Sizes size);

    void deleteAllBySize(Sizes size);

    Wishlist findByUserAndCollectionAndItemAndSize(Users user, Collections collection, Items item, Sizes size);
}