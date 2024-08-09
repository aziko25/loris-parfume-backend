package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Filters.CategoryFilters;
import loris.parfume.DTOs.Requests.Items.CategoriesRequest;
import loris.parfume.DTOs.returnDTOs.CategoriesDTO;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Services.CacheServiceForAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final CacheServiceForAll cacheServiceForAll;

    private final CollectionsRepository collectionsRepository;
    private final ItemsRepository itemsRepository;
    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public CategoriesDTO create(CategoriesRequest categoriesRequest, MultipartFile image) {

        Collections collection = collectionsRepository.findById(categoriesRequest.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        Categories category = Categories.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(categoriesRequest.getNameUz())
                .nameRu(categoriesRequest.getNameRu())
                .nameEng(categoriesRequest.getNameEng())
                .collection(collection)
                .build();

        categoriesRepository.save(category);

        if (image != null && !image.isEmpty()) {

            category.setBannerImage(fileUploadUtilService.handleMediaUpload(category.getId() + "_catBanner", image));
        }

        return new CategoriesDTO(categoriesRepository.save(category));
    }

    public Page<CategoriesDTO> all(Integer page, CategoryFilters filters) {

        if (filters == null) {

            return cacheServiceForAll.allCategories(page);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return categoriesRepository.findAllByFilters(filters.getName(), filters.getCollectionId(), pageable).map(CategoriesDTO::new);
    }

    public CategoriesDTO getById(Long id) {

        return categoriesRepository.findById(id).map(CategoriesDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));
    }

    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public CategoriesDTO update(Long id, CategoriesRequest categoriesRequest, MultipartFile image) {

        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

        Optional.ofNullable(categoriesRequest.getNameUz()).ifPresent(category::setNameUz);
        Optional.ofNullable(categoriesRequest.getNameRu()).ifPresent(category::setNameRu);
        Optional.ofNullable(categoriesRequest.getNameEng()).ifPresent(category::setNameEng);

        if (categoriesRequest.getCollectionId() != null) {

            Collections collection = collectionsRepository.findById(categoriesRequest.getCollectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

            category.setCollection(collection);
        }

        if (category.getBannerImage() != null) {
            fileUploadUtilService.handleMediaDeletion(category.getBannerImage());
        }

        if (image != null && !image.isEmpty()) {

            category.setBannerImage(fileUploadUtilService.handleMediaUpload(category.getId() + "_catBanner", image));
        }

        return new CategoriesDTO(categoriesRepository.save(category));
    }

    @Transactional
    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public String delete(Long id) {

        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

        if (category.getBannerImage() != null) {

            fileUploadUtilService.handleMediaDeletion(category.getBannerImage());
        }

        List<Items> itemsList = itemsRepository.findAllByCategory(category);

        List<Items> batchUpdateItemsList = new ArrayList<>();
        for (Items item : itemsList) {

            item.setCategory(null);
            batchUpdateItemsList.add(item);
        }

        itemsRepository.saveAll(batchUpdateItemsList);

        categoriesRepository.delete(category);

        return "Category Successfully Deleted";
    }
}