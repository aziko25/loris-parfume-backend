package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Orders.DeliveryRates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRatesRepository extends JpaRepository<DeliveryRates, Long> {

    Page<DeliveryRates> findAllByNameLikeIgnoreCase(String s, Pageable pageable);

    DeliveryRates findFirstByIsActive(Boolean isActive);

    DeliveryRates findByIsDefault(boolean b);

    long countByIsActive(boolean b);
}