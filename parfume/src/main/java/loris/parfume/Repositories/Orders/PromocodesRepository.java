package loris.parfume.Repositories.Orders;

import loris.parfume.Models.Orders.Promocodes;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromocodesRepository extends JpaRepository<Promocodes, Long> {

    Optional<Promocodes> findByCode(String code);

    Optional<Promocodes> findByCodeAndIsActiveAndIsDeleted(String code, boolean b, boolean a);

    List<Promocodes> findAllByIsDeleted(Boolean isDeleted, Sort createdTime);
}