package loris.parfume.Services;

import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.CategoriesDTO;
import loris.parfume.DTOs.returnDTOs.ItemsDTO;
import loris.parfume.Models.Banners;
import loris.parfume.Models.CollectionBanners;
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

@Service
@RequiredArgsConstructor
public class CacheServiceForAll {

    private final BannersRepository bannersRepository;
    private final ItemsRepository itemsRepository;
    private final CollectionsRepository collectionsRepository;
    private final CategoriesRepository categoriesRepository;
    private final CollectionBannersRepository collectionBannersRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    @Cacheable(
            value = "bannersCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<Banners> allBanners(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").ascending());

        return bannersRepository.findAll(pageable);
    }

    @Cacheable(
            value = "itemsCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<ItemsDTO> allItems(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz").ascending());

        return itemsRepository.findAll(pageable).map(ItemsDTO::new);
    }

    @Cacheable(
            value = "collectionsCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<Collections> allCollections(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz"));

        return collectionsRepository.findAll(pageable);
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