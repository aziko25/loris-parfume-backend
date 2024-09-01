package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.CategoriesDTO;
import loris.parfume.DTOs.returnDTOs.CollectionsDTO;
import loris.parfume.DTOs.returnDTOs.ItemsDTO;
import loris.parfume.Models.Banners;
import loris.parfume.Models.Branches;
import loris.parfume.Models.CollectionBanners;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Repositories.BannersRepository;
import loris.parfume.Repositories.BranchesRepository;
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
    private final BranchesRepository branchesRepository;

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
            value = "branchesCache",
            key = "'allBranches'"
    )
    public List<Branches> allBranches() {

        return branchesRepository.findAll(Sort.by("createdTime").descending());
    }

    @Cacheable(
            value = "itemsCache",
            key = "'page-'.concat(T(String).valueOf(#page)).concat('-collectionSlug-').concat(#collectionSlug != null ? T(String).valueOf(#collectionSlug) : '').concat('-categorySlug-').concat(#categorySlug != null ? T(String).valueOf(#categorySlug) : '')"
    )
    public Page<ItemsDTO> allItems(Integer page, String collectionSlug, String categorySlug) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz").ascending());

        if (collectionSlug == null) {

            return itemsRepository.findAll(pageable).map(ItemsDTO::new);
        }

        Collections collection = collectionsRepository.findBySlug(collectionSlug)
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        if (categorySlug != null) {

            Categories category = categoriesRepository.findBySlugAndCollection(categorySlug, collection)
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