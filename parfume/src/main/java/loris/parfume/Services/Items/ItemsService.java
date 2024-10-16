package loris.parfume.Services.Items;

import jakarta.persistence.EntityExistsException;
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
import loris.parfume.Services.CacheForAllService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final ItemsRepository itemsRepository;
    private final CacheForAllService cacheForAllService;

    private final CollectionsRepository collectionsRepository;
    private final Collections_Items_Repository collectionsItemsRepository;
    private final CategoriesRepository categoriesRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final Recommended_Items_Repository recommendedItemsRepository;
    private final WishlistRepository wishlistRepository;
    private final BasketsRepository basketsRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final Items_Images_Repository itemsImagesRepository;

    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @Transactional
    @CacheEvict(value = "itemsCache", allEntries = true)
    public ItemsDTO create(ItemsRequest itemsRequest) throws IOException {

        Optional<Items> existingSlug = itemsRepository.findBySlug(itemsRequest.getSlug());
        if (existingSlug.isPresent()) {

            throw new EntityExistsException("Slug Already Exists!");
        }

        if (itemsRequest.getBarcode() != null) {

            Optional<Items> existingBarcode = itemsRepository.findByBarcode(itemsRequest.getBarcode());
            if (existingBarcode.isPresent()) {

                throw new EntityExistsException("Barcode Already Exists!");
            }
        }

        Items item = Items.builder()
                .slug(itemsRequest.getSlug().replace(" ", "-").toLowerCase())
                .barcode(itemsRequest.getBarcode())
                .createdTime(LocalDateTime.now())
                .nameUz(itemsRequest.getNameUz())
                .nameRu(itemsRequest.getNameRu())
                .nameEng(itemsRequest.getNameEng())
                .descriptionUz(itemsRequest.getDescriptionUz())
                .descriptionRu(itemsRequest.getDescriptionRu())
                .descriptionEng(itemsRequest.getDescriptionEng())
                .price(itemsRequest.getPrice())
                .discountPercent(itemsRequest.getDiscountPercent() != null ? itemsRequest.getDiscountPercent() : 0)
                .isRecommendedInMainPage(itemsRequest.getIsRecommendedInMainPage())
                .build();

        itemsRepository.save(item);

        item.setCollectionsItemsList(setItemsCollections(itemsRequest, item));

        if (itemsRequest.getSizesMap() != null && !itemsRequest.getSizesMap().isEmpty()) {

            item.setSizesItemsList(setItemsSizes(itemsRequest, item));
        }

        if (itemsRequest.getCategoryId() != null) {

            Categories category = categoriesRepository.findById(itemsRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

            boolean categoryFoundInCollection = false;
            for (Collections_Items collectionsItem : item.getCollectionsItemsList()) {

                if (category.getCollection().getId().equals(collectionsItem.getCollection().getId())) {

                    item.setCategory(category);
                    categoryFoundInCollection = true;
                    break;
                }
            }

            if (!categoryFoundInCollection) {

                throw new EntityNotFoundException("Category Is Not Found In Given Collections");
            }
        }

        if (itemsRequest.getImagesUrl() != null && !itemsRequest.getImagesUrl().isEmpty()) {

            List<Items_Images> imagesList = new ArrayList<>();

            for (String image : itemsRequest.getImagesUrl()) {

                Items_Images itemsImage = Items_Images.builder()
                        .item(item)
                        .imageName(image)
                        .build();

                imagesList.add(itemsImage);
            }

            item.setItemsImagesList(itemsImagesRepository.saveAll(imagesList));
        }

        return new ItemsDTO(itemsRepository.save(item));
    }

    public Page<ItemsDTO> all(Integer page, String collectionSlug, String categorySlug, ItemFilters itemFilters) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        if (itemFilters.getSearch() == null && itemFilters.getFirstA() == null && itemFilters.getFirstZ() == null &&
            itemFilters.getFirstExpensive() == null && itemFilters.getFirstCheap() == null) {

            return cacheForAllService.allItems(page, collectionSlug, categorySlug);
        }

        return itemsRepository.findAllItemsByFilters(
                itemFilters.getSearch(),
                itemFilters.getFirstA(),
                itemFilters.getFirstZ(),
                itemFilters.getFirstExpensive(),
                itemFilters.getFirstCheap(),
                collectionSlug,
                categorySlug,
                pageable
        ).map(ItemsDTO::new);
    }

    public ItemsDTO getBySlug(String slug) {

        return itemsRepository.findBySlug(slug).map(ItemsDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Item Not Found"));
    }

    @Transactional
    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
    public ItemsDTO update(String slug, ItemsRequest itemsRequest) throws IOException {

        Items item = itemsRepository.findBySlug(slug).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        if (itemsRequest.getSlug() != null) {

            Optional<Items> existingSlug = itemsRepository.findBySlug(itemsRequest.getSlug());
            if (existingSlug.isPresent() && !item.getSlug().equals(existingSlug.get().getSlug())) {

                throw new EntityExistsException("Slug Already Exists!");
            }

            item.setSlug(itemsRequest.getSlug().replace(" ", "-").toLowerCase());
        }

        if (itemsRequest.getBarcode() != null) {

            Optional<Items> existingBarcode = itemsRepository.findByBarcode(itemsRequest.getBarcode());
            if (existingBarcode.isPresent()) {

                throw new EntityExistsException("Barcode Already Exists!");
            }

            item.setBarcode(itemsRequest.getBarcode());
        }

        Optional.ofNullable(itemsRequest.getNameUz()).ifPresent(item::setNameUz);
        Optional.ofNullable(itemsRequest.getNameRu()).ifPresent(item::setNameRu);
        Optional.ofNullable(itemsRequest.getNameEng()).ifPresent(item::setNameEng);

        Optional.ofNullable(itemsRequest.getDescriptionUz()).ifPresent(item::setDescriptionUz);
        Optional.ofNullable(itemsRequest.getDescriptionRu()).ifPresent(item::setDescriptionRu);
        Optional.ofNullable(itemsRequest.getDescriptionEng()).ifPresent(item::setDescriptionEng);

        Optional.ofNullable(itemsRequest.getPrice()).ifPresent(item::setPrice);
        Optional.ofNullable(itemsRequest.getDiscountPercent()).ifPresent(item::setDiscountPercent);
        Optional.ofNullable(itemsRequest.getIsRecommendedInMainPage()).ifPresent(item::setIsRecommendedInMainPage);

        collectionsItemsRepository.deleteAllByItem(item);
        item.setCollectionsItemsList(null);
        if (itemsRequest.getCollectionIds() != null && !itemsRequest.getCollectionIds().isEmpty()) {

            item.setCollectionsItemsList(setItemsCollections(itemsRequest, item));
        }

        item.setCategory(null);
        if (itemsRequest.getCategoryId() != null) {

            Categories category = categoriesRepository.findById(itemsRequest.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

            item.setCategory(category);
        }

        sizesItemsRepository.deleteAllByItem(item);
        item.setSizesItemsList(null);
        if (itemsRequest.getSizesMap() != null && !itemsRequest.getSizesMap().isEmpty()) {

            item.setSizesItemsList(setItemsSizes(itemsRequest, item));
        }

        List<Items_Images> itemsImagesList = itemsImagesRepository.findAllByItem(item);
        List<String> imagesNamesList = itemsImagesList.stream()
                .map(Items_Images::getImageName)
                .toList();

        fileUploadUtilService.handleMultipleMediaDeletion(imagesNamesList);
        itemsImagesRepository.deleteAllByItem(item);
        item.setItemsImagesList(null);

        if (itemsRequest.getImagesUrl() != null && !itemsRequest.getImagesUrl().isEmpty()) {

            List<Items_Images> imagesList = new ArrayList<>();
            for (String image : itemsRequest.getImagesUrl()) {

                if (!imagesNamesList.contains(image)) {

                    Items_Images itemsImage = Items_Images.builder()
                            .item(item)
                            .imageName(image)
                            .build();

                    imagesList.add(itemsImage);
                }
            }

            item.setItemsImagesList(itemsImagesRepository.saveAll(imagesList));
        }

        return new ItemsDTO(itemsRepository.save(item));
    }

    @Transactional
    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
    public String delete(String slug) {

        Items item = itemsRepository.findBySlug(slug).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        sizesItemsRepository.deleteAllByItem(item);
        collectionsItemsRepository.deleteAllByItem(item);
        recommendedItemsRepository.deleteAllByItem(item);
        wishlistRepository.deleteAllByItem(item);
        basketsRepository.deleteAllByItem(item);
        ordersItemsRepository.deleteAllByItem(item);

        List<Items_Images> itemsImagesList = itemsImagesRepository.findAllByItem(item);

        List<String> imagesNamesList = itemsImagesList.stream()
                .map(Items_Images::getImageName)
                .toList();

        fileUploadUtilService.handleMultipleMediaDeletion(imagesNamesList);
        itemsImagesRepository.deleteAllByItem(item);

        itemsRepository.delete(item);

        return "Item Successfully Deleted";
    }

    private List<Sizes_Items> setItemsSizes(ItemsRequest itemsRequest, Items item) {

        List<Sizes_Items> sizesItemsList = new ArrayList<>();

        double cheapestItemPrice = Double.MAX_VALUE;

        for (Map<String, Object> map : itemsRequest.getSizesMap()) {

            Sizes size = sizesRepository.findById(((Number) map.get("id")).longValue())
                    .orElseThrow(() -> new EntityNotFoundException("Size " + map.get("id") + " Not Found"));

            Sizes_Items sizesItem = new Sizes_Items();

            sizesItem.setSize(size);
            sizesItem.setItem(item);
            sizesItem.setPrice((Double) map.get("price"));

            if (cheapestItemPrice > sizesItem.getPrice()) {

                cheapestItemPrice = sizesItem.getPrice();
                item.setDiscountPercent(sizesItem.getDiscountPercent() != null ? sizesItem.getDiscountPercent() : 0);
            }

            if (map.get("discountPercent") != null) {

                sizesItem.setDiscountPercent((Integer) map.get("discountPercent"));
            }
            else {

                sizesItem.setDiscountPercent(0);
            }

            sizesItemsList.add(sizesItem);
        }

        item.setPrice(cheapestItemPrice);

        itemsRepository.save(item);

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