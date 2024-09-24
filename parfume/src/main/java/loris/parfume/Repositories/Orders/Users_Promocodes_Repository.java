package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Orders.Promocodes;
import loris.parfume.Models.Orders.Users_Promocodes;
import loris.parfume.Models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Users_Promocodes_Repository extends JpaRepository<Users_Promocodes, Long> {

    List<Users_Promocodes> findAllByUserAndPromocode(Users user, Promocodes promocode);
}