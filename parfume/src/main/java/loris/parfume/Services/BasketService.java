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
import static loris.parfume.Configurations.Serializers.DoubleSerializer.getFormattedPrice;
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

            if (!collectionsItem.getItem().getSizesItemsList().isEmpty()) {

                throw new EntityNotFoundException("Select Item's Size!");
            }

            size = sizesRepository.findById(DEFAULT_NO_SIZE)
                    .orElseThrow(() -> new EntityNotFoundException("Default Size Not Found"));

            saveBasket(quantity, user, collectionsItem.getItem(), size, collectionsItem.getCollection());

            Sizes_Items sizesItem = Sizes_Items.builder()
                    .item(collectionsItem.getItem())
                    .size(size)
                    .price(collectionsItem.getItem().getPrice())
                    .discountPercent(collectionsItem.getItem().getDiscountPercent())
                    .build();

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

    public Map<String, Object> all() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<Basket> basketList = basketsRepository.findAllByUser(user, Sort.by("addedTime").descending());
        List<BasketDTO> basketDTOList = new ArrayList<>();

        double totalSum = 0.0;
        double totalDiscountedSum = 0.0;

        Map<Long, List<BasketDTO>> collectionMap = new HashMap<>();

        for (Basket basket : basketList) {

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(basket.getItem(), basket.getSize());

            if (sizesItem != null) {

                BasketDTO basketDTO = new BasketDTO(sizesItem, basket.getQuantity(), basket.getCollection());

                basketDTOList.add(basketDTO);

                double itemTotalPrice = basketDTO.getPropPrice() * basketDTO.getQuantity();
                totalSum += itemTotalPrice;

                if (basketDTO.getCollectionId() != null) {
                    collectionMap.computeIfAbsent(basketDTO.getCollectionId(), k -> new ArrayList<>()).add(basketDTO);
                }
                else {
                    totalDiscountedSum += itemTotalPrice; // No discount if not part of a collection
                }
            }
            else {
                basketsRepository.delete(basket);
            }
        }

        // Apply discount logic
        for (Map.Entry<Long, List<BasketDTO>> entry : collectionMap.entrySet()) {
            List<BasketDTO> items = entry.getValue();
            items.sort(Comparator.comparing(BasketDTO::getPropPrice));

            int itemCount = 0;

            for (BasketDTO item : items) {
                itemCount += item.getQuantity();
            }

            int discountedItems = itemCount / 2;

            for (BasketDTO item : items) {
                int currentQuantity = item.getQuantity();

                if (discountedItems > 0) {
                    if (discountedItems >= currentQuantity) {
                        totalDiscountedSum += currentQuantity * item.getPropPrice() * 0.5;
                        discountedItems -= currentQuantity;
                    } else {
                        totalDiscountedSum += discountedItems * item.getPropPrice() * 0.5;
                        totalDiscountedSum += (currentQuantity - discountedItems) * item.getPropPrice();
                        discountedItems = 0;
                    }
                } else {
                    totalDiscountedSum += currentQuantity * item.getPropPrice();
                }
            }
        }

        Map<String, Object> response = new HashMap<>();

        response.put("basketItems", basketDTOList);
        response.put("totalSum", getFormattedPrice(totalSum));
        response.put("totalDiscountedSum", getFormattedPrice(totalDiscountedSum));

        return response;
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