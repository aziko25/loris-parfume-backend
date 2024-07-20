package loris.parfume.Repositories;

import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Users;
import loris.parfume.Models.Wishlist;
import loris.parfume.Models.Wishlist_Ids;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Wishlist_Ids> {

    Page<Wishlist> findAllByUser(Users user, Pageable pageable);

    void deleteByUserAndItem(Users user, Items item);

    void deleteAllByUser(Users user);

    void deleteAllByItem(Items item);
}