package loris.parfume.Services.Orders;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Orders.DeliveryRatesRequest;
import loris.parfume.Models.Orders.DeliveryRates;
import loris.parfume.Repositories.Orders.DeliveryRatesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeliveryRatesService {

    private final DeliveryRatesRepository deliveryRatesRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public DeliveryRates create(DeliveryRatesRequest deliveryRatesRequest) {

        DeliveryRates deliveryRates = new DeliveryRates();

        deliveryRates.setCreatedTime(LocalDateTime.now());
        deliveryRates.setIsActive(deliveryRatesRequest.getIsActive());
        deliveryRates.setName(deliveryRatesRequest.getName());
        deliveryRates.setIsDefault(false);

        if (deliveryRatesRequest.getIsFixed()) {

            deliveryRates.setIsFixed(true);
            deliveryRates.setSumPerKm(deliveryRatesRequest.getSumPerKm());
        }
        else {

            deliveryRates.setIsFixed(false);
            deliveryRates.setFirstFreeKmQuantity(deliveryRatesRequest.getFirstFreeKmQuantity());
            deliveryRates.setAfterFreeKmSumPerKm(deliveryRatesRequest.getAfterFreeKmSumPerKm());
        }

        return deliveryRatesRepository.save(deliveryRates);
    }

    public Page<DeliveryRates> all(Integer page, String search) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        if (search != null) {

            return deliveryRatesRepository.findAllByNameLikeIgnoreCase("%" + search + "%", pageable);
        }

        return deliveryRatesRepository.findAll(pageable);
    }

    public DeliveryRates getById(Long id) {

        return deliveryRatesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Delivery Rate Not Found"));
    }

    public DeliveryRates update(Long id, DeliveryRatesRequest deliveryRatesRequest) {

        DeliveryRates deliveryRate = getById(id);

        if (deliveryRatesRequest.getIsFixed()) {

            deliveryRate.setIsFixed(true);
            Optional.ofNullable(deliveryRatesRequest.getSumPerKm()).ifPresent(deliveryRate::setSumPerKm);
        }
        else {

            deliveryRate.setIsFixed(false);

            Optional.ofNullable(deliveryRatesRequest.getFirstFreeKmQuantity()).ifPresent(deliveryRate::setFirstFreeKmQuantity);
            Optional.ofNullable(deliveryRatesRequest.getAfterFreeKmSumPerKm()).ifPresent(deliveryRate::setAfterFreeKmSumPerKm);
        }

        Optional.ofNullable(deliveryRatesRequest.getName()).ifPresent(deliveryRate::setName);

        Boolean isActive = deliveryRatesRequest.getIsActive();

        if (isActive != null && !isActive) {

            long activeRatesCount = deliveryRatesRepository.countByIsActive(true);

            if (activeRatesCount < 2) {

                throw new IllegalArgumentException("You can't change the status of the only remaining active delivery rate.");
            }

            deliveryRate.setIsActive(false);
        }

        Optional.ofNullable(deliveryRatesRequest.getIsActive()).ifPresent(deliveryRate::setIsActive);

        return deliveryRatesRepository.save(deliveryRate);
    }

    public String delete(Long id) {

        DeliveryRates deliveryRate = getById(id);

        if (deliveryRate.getIsDefault()) {

            throw new IllegalArgumentException("You Can't Delete A Default Delivery Rate");
        }

        deliveryRatesRepository.delete(deliveryRate);

        return "Delivery Rate Deleted";
    }
}