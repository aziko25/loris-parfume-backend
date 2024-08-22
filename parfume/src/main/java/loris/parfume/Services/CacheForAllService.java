package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.CategoriesDTO;
import loris.parfume.DTOs.returnDTOs.CollectionsDTO;
import loris.parfume.DTOs.returnDTOs.ItemsDTO;
import loris.parfume.Models.Banners;
import loris.parfume.Models.CollectionBanners;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Repositories.BannersRepository;
import loris.parfume.Repositories.CollectionBannersRepository;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CacheForAllService {

    private final BannersRepository bannersRepository;
    private final ItemsRepository itemsRepository;
    private final CollectionsRepository collectionsRepository;
    private final CategoriesRepository categoriesRepository;
    private final CollectionBannersRepository collectionBannersRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    @Cacheable(
            value = "bannersCache",
            key = "'allBanners'"
    )
    public List<Banners> allBanners() {

        return bannersRepository.findAll(Sort.by("id").descending());
    }

    @Cacheable(
            value = "itemsCache",
            key = "'page-'.concat(T(String).valueOf(#page)).concat('-collectionId-').concat(#collectionId != null ? T(String).valueOf(#collectionId) : '').concat('-categoryId-').concat(#categoryId != null ? T(String).valueOf(#categoryId) : '')"
    )
    public Page<ItemsDTO> allItems(Integer page, Long collectionId, Long categoryId) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz").ascending());

        if (collectionId == null) {
            return itemsRepository.findAll(pageable).map(ItemsDTO::new);
        }

        Collections collection = collectionsRepository.findById(collectionId)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        if (categoryId != null) {

            Categories category = categoriesRepository.findByIdAndCollection(categoryId, collection)
                    .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

            return itemsRepository.findAllByCollectionsItemsList_CollectionAndCategory(collection, category, pageable)
                    .map(ItemsDTO::new);
        }

        return itemsRepository.findAllByCollectionsItemsList_Collection(collection, pageable).map(ItemsDTO::new);
    }

    @Cacheable(
            value = "collectionsCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<CollectionsDTO> allCollections(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz"));

        return collectionsRepository.findAll(pageable).map(CollectionsDTO::new);
    }

    @Cacheable(
            value = "categoriesCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<CategoriesDTO> allCategories(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz"));

        return categoriesRepository.findAll(pageable).map(CategoriesDTO::new);
    }

    @Cacheable(
            value = "collectionsBannersCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<CollectionBanners> allCollectionsBanners(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").ascending());

        return collectionBannersRepository.findAll(pageable);
    }
}