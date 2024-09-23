package loris.parfume.Services.Orders;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Orders.PromocodeRequest;
import loris.parfume.Models.Orders.Promocodes;
import loris.parfume.Repositories.Orders.PromocodesRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromocodesService {

    private final PromocodesRepository promocodesRepository;

    public Promocodes create(PromocodeRequest promocodeRequest) {

        Optional<Promocodes> existingCode = promocodesRepository.findByCode(promocodeRequest.getCode().toUpperCase());

        if (existingCode.isPresent()) {

            throw new EntityExistsException("This Promocode Already Exists!");
        }

        Promocodes promocodes = Promocodes.builder()
                .createdTime(LocalDateTime.now())
                .code(promocodeRequest.getCode().toUpperCase())
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

        if (!promocode.getIsForever()) {

            if (LocalDateTime.now().isBefore(promocode.getStartTime()) || LocalDateTime.now().isAfter(promocode.getEndTime())) {

                throw new EntityNotFoundException("Promocode Doesn't Exist!");
            }
        }

        // check users activation

        return promocode;
    }

    public String delete(Long id) {

        Promocodes promocode = promocodesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promocode Doesn't Exist!"));

        promocodesRepository.delete(promocode);

        return "Promocode Deleted!";
    }
}