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

        Optional<Promocodes> existingCode = promocodesRepository.findByCode(promocodeRequest.getCode().toUpperCase().replace(" ", ""));

        if (existingCode.isPresent()) {

            throw new EntityExistsException("This Promocode Already Exists!");
        }

        Promocodes promocodes = Promocodes.builder()
                .createdTime(LocalDateTime.now())
                .code(promocodeRequest.getCode().toUpperCase().replace(" ", ""))
                .isActive(promocodeRequest.getIsActive())
                .isDeleted(false)
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
        promocodes.setUserActivationQuantity(1);
        if (!promocodeRequest.getIsUserActivationOnce()) {

            promocodes.setIsUserActivationOnce(false);
            promocodes.setUserActivationQuantity(promocodeRequest.getUserActivationQuantity());
        }

        return promocodesRepository.save(promocodes);
    }

    public List<Promocodes> all() {

        return promocodesRepository.findAllByIsDeleted(false, Sort.by("createdTime").descending());
    }

    public Promocodes getByCode(String code) {

        Promocodes promocode = promocodesRepository.findByCodeAndIsActiveAndIsDeleted(code.toUpperCase(), true, false)
                .orElseThrow(() -> new EntityNotFoundException("Promocode Doesn't Exist!"));

        if (!promocode.getIsEndlessQuantity()) {

            if (promocode.getActivationQuantity() <= promocode.getActivatedQuantity()) {

                throw new EntityNotFoundException("Promocode Doesn't Exist!");
            }
        }

        if (!promocode.getIsForever()) {

            if (LocalDateTime.now().isBefore(promocode.getStartTime()) || LocalDateTime.now().isAfter(promocode.getEndTime())) {

                throw new EntityNotFoundException("Promocode Doesn't Exist!");
            }
        }

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Doesn't Exist!"));

        List<Users_Promocodes> usersPromocode = usersPromocodesRepository.findAllByUserAndPromocode(user, promocode);

        if (usersPromocode != null && !usersPromocode.isEmpty() && promocode.getIsUserActivationOnce()) {

            throw new EntityExistsException("Promocode Was Already Activated!");
        }

        if (usersPromocode != null && usersPromocode.size() >= promocode.getUserActivationQuantity()) {

            throw new EntityExistsException("Promocode Was Already Activated!");
        }

        return promocode;
    }

    public Promocodes update(Long promocodeId, PromocodeRequest promocodeRequest) {

        Promocodes promocodes = promocodesRepository.findById(promocodeId)
                .orElseThrow(() -> new EntityNotFoundException("Promocode not found with id: " + promocodeId));

        Optional<Promocodes> existingCode = promocodesRepository.findByCode(promocodeRequest.getCode().toUpperCase().replace(" ", ""));
        if (existingCode.isPresent() && !existingCode.get().getId().equals(promocodeId)) {
            throw new EntityExistsException("This Promocode Already Exists!");
        }

        promocodes.setCode(promocodeRequest.getCode().toUpperCase().replace(" ", ""));
        promocodes.setIsActive(promocodeRequest.getIsActive());
        promocodes.setDiscountPercent(promocodeRequest.getDiscountPercent());
        promocodes.setDiscountSum(promocodeRequest.getDiscountSum());

        // Handle endless quantity
        promocodes.setIsEndlessQuantity(promocodeRequest.getIsEndlessQuantity());
        if (!promocodeRequest.getIsEndlessQuantity()) {
            promocodes.setActivationQuantity(promocodeRequest.getActivationQuantity());
        } else {
            promocodes.setActivationQuantity(null);
        }

        // Handle time-bound validity
        promocodes.setIsForever(promocodeRequest.getIsForever());
        if (!promocodeRequest.getIsForever()) {
            promocodes.setStartTime(promocodeRequest.getStartTime());
            promocodes.setEndTime(promocodeRequest.getEndTime());
        } else {
            promocodes.setStartTime(null);
            promocodes.setEndTime(null);
        }

        // Handle user activation limits
        promocodes.setIsUserActivationOnce(promocodeRequest.getIsUserActivationOnce());
        if (!promocodeRequest.getIsUserActivationOnce()) {
            promocodes.setUserActivationQuantity(promocodeRequest.getUserActivationQuantity());
        } else {
            promocodes.setUserActivationQuantity(1);
        }

        return promocodesRepository.save(promocodes);
    }

    public String delete(Long id) {

        Promocodes promocode = promocodesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promocode Doesn't Exist!"));

        promocode.setIsDeleted(true);
        promocodesRepository.save(promocode);

        return "Promocode Deleted!";
    }

    public void activatePromocode(Users user, Promocodes promocode) {

        promocode.setActivatedQuantity(promocode.getActivatedQuantity() + 1);
        promocodesRepository.save(promocode);

        Users_Promocodes usersPromocode = new Users_Promocodes();

        usersPromocode.setPromocode(promocode);
        usersPromocode.setUser(user);
        usersPromocodesRepository.save(usersPromocode);
    }
}