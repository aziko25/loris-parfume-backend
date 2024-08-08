package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.BasketDTO;
import loris.parfume.Models.Basket;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;
import static loris.parfume.DefaultEntitiesService.DEFAULT_NO_SIZE;

@Service
@RequiredArgsConstructor
public class BasketService {

    private final BasketsRepository basketsRepository;

    private final UsersRepository usersRepository;
    private final ItemsRepository itemsRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;

    public BasketDTO add(Long itemId, Long sizeId, Integer quantity) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));
        Sizes size;

        if (sizeId != null) {

            size = sizesRepository.findById(sizeId)
                    .orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(item, size);

            if (sizesItem == null) {

                throw new EntityNotFoundException("Item With This Size Not Found");
            }

            Integer totalQuantity = saveBasket(quantity, user, item, size, sizesItem.getPrice(), sizesItem.getDiscountPercent());

            return new BasketDTO(sizesItem, totalQuantity);
        }
        else {

            if (!item.getSizesItemsList().isEmpty()) {

                throw new EntityNotFoundException("Select Item's Size!");
            }

            size = sizesRepository.findById(DEFAULT_NO_SIZE)
                    .orElseThrow(() -> new EntityNotFoundException("Default Size Not Found"));

            Integer totalQuantity = saveBasket(quantity, user, item, size, item.getPrice(), item.getDiscountPercent());

            Sizes_Items sizesItem = Sizes_Items.builder()
                    .item(item)
                    .size(size)
                    .quantity(totalQuantity)
                    .price(item.getPrice())
                    .discountPercent(item.getDiscountPercent())
                    .build();

            return new BasketDTO(sizesItem, quantity);
        }
    }

    private Integer saveBasket(Integer quantity, Users user, Items item, Sizes size, Double price, Integer discountPercent) {

        Basket basket = basketsRepository.findByUserAndItemAndSize(user, item, size);

        if (basket == null) {

            basket = new Basket(user, item, size, quantity, price, discountPercent, LocalDateTime.now());
        }
        else {

            basket.setQuantity(basket.getQuantity() + quantity);
        }

        basketsRepository.save(basket);

        return basket.getQuantity();
    }

    public List<BasketDTO> all() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<Basket> basketList = basketsRepository.findAllByUser(user, Sort.by("addedTime").descending());
        List<BasketDTO> basketDTOList = new ArrayList<>();

        for (Basket basket : basketList) {

            Sizes_Items sizesItems = Sizes_Items.builder()
                    .item(basket.getItem())
                    .size(basket.getSize())
                    .quantity(basket.getQuantity())
                    .price(basket.getPrice())
                    .discountPercent(basket.getDiscountPercent())
                    .build();

            basketDTOList.add(new BasketDTO(sizesItems, basket.getQuantity()));
        }

        return basketDTOList;
    }

    @Transactional
    public String remove(Long itemId, Long sizeId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        if (sizeId != null) {

            Sizes size = sizesRepository.findById(sizeId).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

            basketsRepository.deleteByUserAndItemAndSize(user, item, size);
        }
        else {

            basketsRepository.deleteByUserAndItem(user, item);
        }

        return "Successfully Deleted";
    }

    @Transactional
    public String clear() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        basketsRepository.deleteAllByUser(user);

        return "Successfully Cleared The Cart";
    }
}