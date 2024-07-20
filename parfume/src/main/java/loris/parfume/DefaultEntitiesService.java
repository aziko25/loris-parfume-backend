package loris.parfume;

import lombok.RequiredArgsConstructor;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Orders.DeliveryRates;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Orders.DeliveryRatesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DefaultEntitiesService {

    private final SizesRepository sizesRepository;
    private final DeliveryRatesRepository deliveryRatesRepository;

    public static Long DEFAULT_NO_SIZE;
    public static DeliveryRates DELIVERY_RATE;

    // Creates default no size for items that doesn't have sizes, so it must not be deleted
    @Bean
    public void createDefaultSize() {

        System.out.println("Running size default");

        Sizes size = sizesRepository.findByIsDefaultNoSize(true);

        if (size == null) {

            size = Sizes.builder()
                    .createdTime(LocalDateTime.now())
                    .nameUz("Razmeri Yo'q")
                    .nameRu("Нет Размеров")
                    .nameEng("No Sizes")
                    .isDefaultNoSize(true)
                    .build();

            sizesRepository.save(size);
        }

        DEFAULT_NO_SIZE = size.getId();
    }

    @Bean
    public void createDefaultRate() {

        DeliveryRates deliveryRate = deliveryRatesRepository.findByIsDefault(true);

        if (deliveryRate == null) {

            deliveryRate = new DeliveryRates();

            deliveryRate.setCreatedTime(LocalDateTime.now());
            deliveryRate.setName("Дефолтный Тариф");
            deliveryRate.setIsDefault(true);
            deliveryRate.setIsFixed(true);
            deliveryRate.setSumPerKm(2000.0);
            deliveryRate.setIsActive(true);

            deliveryRatesRepository.save(deliveryRate);
        }

        DELIVERY_RATE = deliveryRate;
    }
}