package loris.parfume.Services.Items;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Requests.Items.CollectionsRequest;
import loris.parfume.DTOs.returnDTOs.CollectionsDTO;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.Collections_Items_Repository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import loris.parfume.Services.CacheForAllService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final CacheForAllService cacheForAllService;

    private final CategoriesRepository categoriesRepository;
    private final Collections_Items_Repository collectionsItemsRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public CollectionsDTO create(CollectionsRequest collectionsRequest) throws IOException {

        Optional<Collections> existingSlug = collectionsRepository.findBySlug(collectionsRequest.getSlug());
        if (existingSlug.isPresent()) {

            throw new EntityExistsException(existingSlug.get().getSlug() + " Already Exists!");
        }

        Collections collection = Collections.builder()
                .slug(collectionsRequest.getSlug().replace(" ", "-").toLowerCase())
                .createdTime(LocalDateTime.now())
                .nameUz(collectionsRequest.getNameUz())
                .nameRu(collectionsRequest.getNameRu())
                .nameEng(collectionsRequest.getNameEng())
                .descriptionUz(collectionsRequest.getDescriptionUz())
                .descriptionRu(collectionsRequest.getDescriptionRu())
                .bannerImage(collectionsRequest.getImageUrl())
                .sortOrder(collectionsRequest.getSortOrder())
                .isFiftyPercentSaleApplied(collectionsRequest.getIsFiftyPercentSaleApplied())
                .isRecommendedInMainPage(collectionsRequest.getIsRecommendedInMainPage())
                .build();

        return new CollectionsDTO(collectionsRepository.save(collection));
    }

    public Page<CollectionsDTO> all(Integer page, String name) {

        if (name == null) {

            return cacheForAllService.allCollections(page);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("sortOrder").ascending());

        return collectionsRepository.findAllByAnyNameLikeIgnoreCase("%" + name + "%", pageable).map(CollectionsDTO::new);
    }

    public CollectionsDTO getBySlug(String slug) {

        return collectionsRepository.findBySlug(slug)
                .map(CollectionsDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));
    }

    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public CollectionsDTO update(String slug, CollectionsRequest collectionsRequest) throws IOException {

        Collections collection = collectionsRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        if (collectionsRequest != null) {

            if (collectionsRequest.getSlug() != null) {

                Optional<Collections> existingSlug = collectionsRepository.findBySlug(collectionsRequest.getSlug());
                if (existingSlug.isPresent() && !existingSlug.get().getId().equals(collection.getId())) {

                    throw new EntityExistsException(existingSlug.get().getSlug() + " Already Exists!");
                }

                collection.setSlug(collectionsRequest.getSlug().replace(" ", "-").toLowerCase());
            }

            Optional.ofNullable(collectionsRequest.getNameUz()).ifPresent(collection::setNameUz);
            Optional.ofNullable(collectionsRequest.getNameRu()).ifPresent(collection::setNameRu);
            Optional.ofNullable(collectionsRequest.getNameEng()).ifPresent(collection::setNameEng);
            Optional.ofNullable(collectionsRequest.getDescriptionUz()).ifPresent(collection::setDescriptionUz);
            Optional.ofNullable(collectionsRequest.getDescriptionRu()).ifPresent(collection::setDescriptionRu);
            Optional.ofNullable(collectionsRequest.getSortOrder()).ifPresent(collection::setSortOrder);
            Optional.ofNullable(collectionsRequest.getIsRecommendedInMainPage()).ifPresent(collection::setIsRecommendedInMainPage);
            Optional.ofNullable(collectionsRequest.getImageUrl()).ifPresent(collection::setBannerImage);
        }

        return new CollectionsDTO(collectionsRepository.save(collection));
    }

    @Transactional
    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public String delete(String slug) {

        Collections collection = collectionsRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        fileUploadUtilService.handleMediaDeletion(collection.getBannerImage());

        List<Categories> categoriesList = categoriesRepository.findAllByCollection(collection);
        List<Categories> batchUpdateCategoriesList = new ArrayList<>();

        for (Categories category : categoriesList) {

            category.setCollection(null);
            batchUpdateCategoriesList.add(category);
        }

        categoriesRepository.saveAll(batchUpdateCategoriesList);

        List<Collections_Items> collectionsItemsList = collectionsItemsRepository.findAllByCollection(collection);

        collectionsItemsRepository.deleteAll(collectionsItemsList);

        List<Orders_Items> ordersItemsList = ordersItemsRepository.findAllByCollection(collection);
        List<Orders_Items> batchUpdateOrdersItemsList = new ArrayList<>();
        for (Orders_Items ordersItem : ordersItemsList) {

            ordersItem.setCollection(null);
            batchUpdateOrdersItemsList.add(ordersItem);
        }
        ordersItemsRepository.saveAll(batchUpdateOrdersItemsList);

        collectionsRepository.delete(collection);

        return "Collection Deleted Successfully";
    }
}