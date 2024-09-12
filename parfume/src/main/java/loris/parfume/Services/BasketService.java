package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.BasketDTO;
import loris.parfume.Models.Basket;
import loris.parfume.Models.Items.*;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.Items.*;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
    private final Collections_Items_Repository collectionsItemsRepository;

    public BasketDTO add(String itemSlug, Long sizeId, String collectionSlug, Integer quantity) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Collections_Items collectionsItem = collectionsItemsRepository
                .findByCollectionSlugAndItemSlug(collectionSlug, itemSlug)
                .orElseThrow(() -> new EntityNotFoundException("Item: " + itemSlug + " Does Not Belong To This Collection!"));

        Sizes size;

        if (sizeId != null) {

            size = sizesRepository.findById(sizeId)
                    .orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(collectionsItem.getItem(), size);

            if (sizesItem == null) {

                throw new EntityNotFoundException("Item With This Size Not Found");
            }

            Integer totalQuantity = saveBasket(quantity, user, collectionsItem.getItem(), size, collectionsItem.getCollection());

            return new BasketDTO(sizesItem, totalQuantity, collectionsItem.getCollection());
        }
        else {

            size = sizesRepository.findById(DEFAULT_NO_SIZE)
                    .orElseThrow(() -> new EntityNotFoundException("Default Size Not Found"));

            boolean sizeFound = false;

            if (!collectionsItem.getItem().getSizesItemsList().isEmpty()) {

                for (Sizes_Items sizesItem : collectionsItem.getItem().getSizesItemsList()) {

                    if (sizesItem.getSize().getId().equals(size.getId())) {

                        sizeFound = true;
                        break;
                    }
                }

                if (!sizeFound) {
                    throw new EntityNotFoundException("Select Item's Size!");
                }
            }

            saveBasket(quantity, user, collectionsItem.getItem(), size, collectionsItem.getCollection());

            Sizes_Items sizesItem = Sizes_Items.builder()
                    .item(collectionsItem.getItem())
                    .size(size)
                    .price(collectionsItem.getItem().getPrice())
                    .discountPercent(collectionsItem.getItem().getDiscountPercent())
                    .build();

            sizesItemsRepository.save(sizesItem);

            return new BasketDTO(sizesItem, quantity, collectionsItem.getCollection());
        }
    }

    private Integer saveBasket(Integer quantity, Users user, Items item, Sizes size, Collections collection) {

        Basket basket = basketsRepository.findByUserAndItemAndSize(user, item, size);

        if (basket == null) {

            basket = new Basket(user, item, size, collection, LocalDateTime.now(), quantity);
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

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(basket.getItem(), basket.getSize());

            if (sizesItem != null) {

                basketDTOList.add(new BasketDTO(sizesItem, basket.getQuantity(), basket.getCollection()));
            }
            else {

                basketsRepository.delete(basket);
            }
        }

        return basketDTOList;
    }

    @Transactional
    public String remove(String itemSlug, Long sizeId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findBySlug(itemSlug).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

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