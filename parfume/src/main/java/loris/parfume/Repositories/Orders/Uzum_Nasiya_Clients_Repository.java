package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Orders.Uzum_Nasiya_Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Uzum_Nasiya_Clients_Repository extends JpaRepository<Uzum_Nasiya_Clients, Long> {

    Optional<Uzum_Nasiya_Clients> findByOrder(Orders order);
}