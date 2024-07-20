package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Filters.ItemFilters;
import loris.parfume.DTOs.Requests.Items.ItemsRequest;
import loris.parfume.DTOs.returnDTOs.ItemsDTO;
import loris.parfume.Models.Items.*;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.Items.*;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import loris.parfume.Repositories.WishlistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final ItemsRepository itemsRepository;

    private final CollectionsRepository collectionsRepository;
    private final Collections_Items_Repository collectionsItemsRepository;
    private final CategoriesRepository categoriesRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final Recommended_Items_Repository recommendedItemsRepository;
    private final WishlistRepository wishlistRepository;
    private final BasketsRepository basketsRepository;
    private final Orders_Items_Repository ordersItemsRepository;

    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @Transactional
    public ItemsDTO create(MultipartFile image, ItemsRequest itemsRequest) {

        Items item = Items.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(itemsRequest.getNameUz())
                .nameRu(itemsRequest.getNameRu())
                .nameEng(itemsRequest.getNameEng())
                .descriptionUz(itemsRequest.getDescriptionUz())
                .descriptionRu(itemsRequest.getDescriptionRu())
                .descriptionEng(itemsRequest.getDescriptionEng())
                .quantity(itemsRequest.getQuantity())
                .price(itemsRequest.getPrice())
                .discountPercent(itemsRequest.getDiscountPercent())
                .build();
        itemsRepository.save(item);

        item.setCollectionsItemsList(setItemsCollections(itemsRequest, item));

        if (itemsRequest.getSizesMap() != null && !itemsRequest.getSizesMap().isEmpty()) {

            item.setSizesItemsList(setItemsSizes(itemsRequest, item));
        }

        if (itemsRequest.getCategoryId() != null) {

            Categories category = categoriesRepository.findById(itemsRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

            item.setCategory(category);
        }

        if (image != null && !image.isEmpty()) {

            item.setImageName(fileUploadUtilService.handleMediaUpload(item.getId() + "_item", image));
        }

        return new ItemsDTO(itemsRepository.save(item));
    }

    public Page<ItemsDTO> all(Integer page, ItemFilters itemFilters) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("name").ascending());

        if (itemFilters != null) {

            pageable = PageRequest.of(page - 1, pageSize);

            return itemsRepository.findAllItemsByFilters(itemFilters.getSearch(), itemFilters.getFirstA(),
                            itemFilters.getFirstZ(), itemFilters.getFirstExpensive(), itemFilters.getFirstCheap(), pageable)
                    .map(ItemsDTO::new);
        }

        return itemsRepository.findAll(pageable).map(ItemsDTO::new);
    }

    public ItemsDTO getById(Long id) {

        return itemsRepository.findById(id).map(ItemsDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Item Not Found"));
    }

    @Transactional
    public ItemsDTO update(Long id, MultipartFile image, ItemsRequest itemsRequest) {

        Items item = itemsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        Optional.ofNullable(itemsRequest.getNameUz()).ifPresent(item::setNameUz);
        Optional.ofNullable(itemsRequest.getNameRu()).ifPresent(item::setNameRu);
        Optional.ofNullable(itemsRequest.getNameEng()).ifPresent(item::setNameEng);

        Optional.ofNullable(itemsRequest.getDescriptionUz()).ifPresent(item::setDescriptionUz);
        Optional.ofNullable(itemsRequest.getDescriptionRu()).ifPresent(item::setDescriptionRu);
        Optional.ofNullable(itemsRequest.getDescriptionEng()).ifPresent(item::setDescriptionEng);

        Optional.ofNullable(itemsRequest.getQuantity()).ifPresent(item::setQuantity);
        Optional.ofNullable(itemsRequest.getPrice()).ifPresent(item::setPrice);
        Optional.ofNullable(itemsRequest.getDiscountPercent()).ifPresent(item::setDiscountPercent);

        if (itemsRequest.getCollectionIds() != null && !itemsRequest.getCollectionIds().isEmpty()) {

            collectionsItemsRepository.deleteAllByItem(item);

            item.setCollectionsItemsList(setItemsCollections(itemsRequest, item));
        }

        if (itemsRequest.getCategoryId() != null) {

            Categories category = categoriesRepository.findById(itemsRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

            item.setCategory(category);
        }

        if (itemsRequest.getSizesMap() != null && !itemsRequest.getSizesMap().isEmpty()) {

            sizesItemsRepository.deleteAllByItem(item);

            item.setSizesItemsList(setItemsSizes(itemsRequest, item));
        }

        if (image != null && !image.isEmpty()) {

            item.setImageName(fileUploadUtilService.handleMediaUpload(item.getId() + "_item", image));
        }
        else {

            fileUploadUtilService.handleMediaDeletion(item.getImageName());
            item.setImageName(null);
        }

        return new ItemsDTO(itemsRepository.save(item));
    }

    @Transactional
    public String delete(Long id) {

        Items item = itemsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        sizesItemsRepository.deleteAllByItem(item);
        collectionsItemsRepository.deleteAllByItem(item);
        recommendedItemsRepository.deleteAllByItem(item);
        wishlistRepository.deleteAllByItem(item);
        basketsRepository.deleteAllByItem(item);
        ordersItemsRepository.deleteAllByItem(item);

        fileUploadUtilService.handleMediaDeletion(item.getImageName());

        itemsRepository.delete(item);

        return "Item Successfully Deleted";
    }

    private List<Sizes_Items> setItemsSizes(ItemsRequest itemsRequest, Items item) {

        List<Sizes_Items> sizesItemsList = new ArrayList<>();

        for (Map<String, Object> map : itemsRequest.getSizesMap()) {

            Sizes size = sizesRepository.findById(((Number) map.get("id")).longValue())
                    .orElseThrow(() -> new EntityNotFoundException("Size " + map.get("id") + " Not Found"));

            Sizes_Items sizesItem = new Sizes_Items();

            sizesItem.setSize(size);
            sizesItem.setItem(item);
            sizesItem.setPrice((Double) map.get("price"));

            if (map.get("quantity") != null) {
                sizesItem.setQuantity((Integer) map.get("quantity"));
            }

            if (map.get("discountPercent") != null) {
                sizesItem.setDiscountPercent((Integer) map.get("discountPercent"));
            }

            sizesItemsList.add(sizesItem);
        }

        return sizesItemsRepository.saveAll(sizesItemsList);
    }

    private List<Collections_Items> setItemsCollections(ItemsRequest itemsRequest, Items item) {

        List<Collections_Items> collectionsItemsList = new ArrayList<>();

        for (Long collectionId : itemsRequest.getCollectionIds()) {

            Collections collection = collectionsRepository.findById(collectionId)
                    .orElseThrow(() -> new EntityNotFoundException("Collection " + collectionId + " Not Found"));

            Collections_Items collectionsItems = new Collections_Items();

            collectionsItems.setCollection(collection);
            collectionsItems.setItem(item);

            collectionsItemsList.add(collectionsItems);
        }

        return collectionsItemsRepository.saveAll(collectionsItemsList);
    }
}