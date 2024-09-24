package loris.parfume.Services.Orders;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Orders.PromocodeRequest;
import loris.parfume.Models.Orders.Promocodes;
import loris.parfume.Models.Orders.Users_Promocodes;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.Orders.PromocodesRepository;
import loris.parfume.Repositories.Orders.Users_Promocodes_Repository;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;

@Service
@RequiredArgsConstructor
public class PromocodesService {

    private final PromocodesRepository promocodesRepository;
    private final Users_Promocodes_Repository usersPromocodesRepository;
    private final UsersRepository usersRepository;

    public Promocodes create(PromocodeRequest promocodeRequest) {

        Optional<Promocodes> existingCode = promocodesRepository.findByCode(promocodeRequest.getCode().toUpperCase());

        if (existingCode.isPresent()) {

            throw new EntityExistsException("This Promocode Already Exists!");
        }

        Promocodes promocodes = Promocodes.builder()
                .createdTime(LocalDateTime.now())
                .code(promocodeRequest.getCode().toUpperCase().replace(" ", ""))
                .isActive(promocodeRequest.getIsActive())
                .discountPercent(promocodeRequest.getDiscountPercent())
                .discountSum(promocodeRequest.getDiscountSum())
                .activatedQuantity(0)
                .build();

        promocodes.setIsEndlessQuantity(true);
        if (!promocodeRequest.getIsEndlessQuantity()) {

            promocodes.setIsEndlessQuantity(false);
            promocodes.setActivationQuantity(promocodeRequest.getActivationQuantity());
        }

        promocodes.setIsForever(true);
        if (!promocodeRequest.getIsForever()) {

            promocodes.setIsForever(false);
            promocodes.setStartTime(promocodeRequest.getStartTime());
            promocodes.setEndTime(promocodeRequest.getEndTime());
        }

        promocodes.setIsUserActivationOnce(true);
        if (!promocodeRequest.getIsUserActivationOnce()) {

            promocodes.setIsUserActivationOnce(false);
            promocodes.setUserActivationQuantity(promocodeRequest.getUserActivationQuantity());
        }

        return promocodesRepository.save(promocodes);
    }

    public List<Promocodes> all() {

        return promocodesRepository.findAll(Sort.by("createdTime").descending());
    }

    public Promocodes getByCode(String code) {

        Promocodes promocode = promocodesRepository.findByCodeAndIsActive(code.toUpperCase(), true)
                .orElseThrow(() -> new EntityNotFoundException("Promocode Doesn't Exist!"));

        if (!promocode.getIsEndlessQuantity()) {

            if (promocode.getActivationQuantity() <= promocode.getActivatedQuantity()) {

                throw new EntityNotFoundException("Promocode Doesn't Exist!");
            }
        }
        else if (promocode.getActivationQuantity() >= promocode.getActivatedQuantity()) {

            throw new EntityNotFoundException("Promocode Doesn't Exist!");
        }

        if (!promocode.getIsForever()) {

            if (LocalDateTime.now().isBefore(promocode.getStartTime()) || LocalDateTime.now().isAfter(promocode.getEndTime())) {

                throw new EntityNotFoundException("Promocode Doesn't Exist!");
            }
        }

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Doesn't Exist!"));

        List<Users_Promocodes> usersPromocode = usersPromocodesRepository.findAllByUserAndPromocode(user, promocode);

        if (usersPromocode != null && usersPromocode.size() > 1 && promocode.getIsUserActivationOnce()) {

            throw new IllegalArgumentException("Promocode Was Already Activated!");
        }

        if (usersPromocode != null && usersPromocode.size() >= promocode.getUserActivationQuantity()) {

            throw new IllegalArgumentException("Promocode Was Already Activated!");
        }

        return promocode;
    }

    public String delete(Long id) {

        Promocodes promocode = promocodesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promocode Doesn't Exist!"));

        promocodesRepository.delete(promocode);

        return "Promocode Deleted!";
    }
}