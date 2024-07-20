package loris.parfume.Repositories.Orders;

import loris.parfume.DTOs.returnDTOs.OrdersDTO;
import loris.parfume.Models.Orders.Orders;
import loris.parfume.Models.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {

    Page<Orders> findAllByUser(Users user, Pageable pageable);

    Optional<Orders> findByIdAndUser(Long id, Users user);
}