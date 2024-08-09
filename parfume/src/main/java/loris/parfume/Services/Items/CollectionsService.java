package loris.parfume.Services.Items;

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
import loris.parfume.Services.CacheServiceForAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final CacheServiceForAll cacheServiceForAll;

    private final CategoriesRepository categoriesRepository;
    private final Collections_Items_Repository collectionsItemsRepository;
    private final Orders_Items_Repository ordersItemsRepository;
    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public CollectionsDTO create(CollectionsRequest collectionsRequest, MultipartFile image) {

        Collections collection = Collections.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(collectionsRequest.getNameUz())
                .nameRu(collectionsRequest.getNameRu())
                .nameEng(collectionsRequest.getNameEng())
                .build();

        collectionsRepository.save(collection);

        collection.setBannerImage(fileUploadUtilService.handleMediaUpload(collection.getId() + "_collBanner", image));

        return new CollectionsDTO(collectionsRepository.save(collection));
    }

    public Page<CollectionsDTO> all(Integer page, String name) {

        if (name == null) {

            return cacheServiceForAll.allCollections(page);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return collectionsRepository.findAllByAnyNameLikeIgnoreCase("%" + name + "%", pageable).map(CollectionsDTO::new);
    }

    public CollectionsDTO getById(Long id) {

        return collectionsRepository.findById(id)
                .map(CollectionsDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));
    }

    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public CollectionsDTO update(Long id, CollectionsRequest collectionsRequest, MultipartFile image) {

        Collections collection = collectionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        if (collectionsRequest != null) {

            Optional.ofNullable(collectionsRequest.getNameUz()).ifPresent(collection::setNameUz);
            Optional.ofNullable(collectionsRequest.getNameRu()).ifPresent(collection::setNameRu);
            Optional.ofNullable(collectionsRequest.getNameEng()).ifPresent(collection::setNameEng);
        }

        fileUploadUtilService.handleMediaDeletion(collection.getBannerImage());

        if (image != null && !image.isEmpty()) {

            collection.setBannerImage(fileUploadUtilService.handleMediaUpload(collection.getId() + "_collBanner", image));
        }

        return new CollectionsDTO(collectionsRepository.save(collection));
    }

    @Transactional
    @CacheEvict(value = {"collectionsCache", "categoriesCache", "itemsCache"}, allEntries = true)
    public String delete(Long id) {

        Collections collection = collectionsRepository.findById(id)
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