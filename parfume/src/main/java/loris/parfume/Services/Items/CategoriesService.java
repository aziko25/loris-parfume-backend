package loris.parfume.Services.Items;

import jakarta.persistence.EntityExistsException;
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
import loris.parfume.Services.CacheForAllService;
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
    private final CacheForAllService cacheForAllService;

    private final CollectionsRepository collectionsRepository;
    private final ItemsRepository itemsRepository;
    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public CategoriesDTO create(CategoriesRequest categoriesRequest, MultipartFile image) {

        Collections collection = collectionsRepository.findById(categoriesRequest.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        Optional<Categories> existingSlug = categoriesRepository.findBySlug(categoriesRequest.getSlug());
        if (existingSlug.isPresent()) {

            throw new EntityExistsException("Slug already exists");
        }

        Categories category = Categories.builder()
                .slug(categoriesRequest.getSlug().replace(" ", "-").toLowerCase())
                .createdTime(LocalDateTime.now())
                .nameUz(categoriesRequest.getNameUz())
                .nameRu(categoriesRequest.getNameRu())
                .nameEng(categoriesRequest.getNameEng())
                .isRecommendedInMainPage(categoriesRequest.getIsRecommendedInMainPage())
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

            return cacheForAllService.allCategories(page);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return categoriesRepository.findAllByFilters(filters.getName(), filters.getCollectionId(), pageable).map(CategoriesDTO::new);
    }

    public CategoriesDTO getBySlug(String slug) {

        return categoriesRepository.findBySlug(slug).map(CategoriesDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));
    }

    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public CategoriesDTO update(String slug, CategoriesRequest categoriesRequest, MultipartFile image) {

        Categories category = categoriesRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

        if (categoriesRequest.getSlug() != null) {

            Optional<Categories> existingSlug = categoriesRepository.findBySlug(categoriesRequest.getSlug());

            if (existingSlug.isPresent() && !existingSlug.get().getId().equals(category.getId())) {

                throw new EntityExistsException("Slug already exists");
            }

            category.setSlug(categoriesRequest.getSlug().replace(" ", "-").toLowerCase());
        }

        Optional.ofNullable(categoriesRequest.getNameUz()).ifPresent(category::setNameUz);
        Optional.ofNullable(categoriesRequest.getNameRu()).ifPresent(category::setNameRu);
        Optional.ofNullable(categoriesRequest.getNameEng()).ifPresent(category::setNameEng);
        Optional.ofNullable(categoriesRequest.getIsRecommendedInMainPage()).ifPresent(category::setIsRecommendedInMainPage);

        if (categoriesRequest.getCollectionId() != null) {

            Collections collection = collectionsRepository.findById(categoriesRequest.getCollectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

            category.setCollection(collection);
        }
        else {
            category.setCollection(null);
        }

        if (image != null && !image.isEmpty()) {

            if (category.getBannerImage() != null) {
                fileUploadUtilService.handleMediaDeletion(category.getBannerImage());
            }

            category.setBannerImage(fileUploadUtilService.handleMediaUpload(category.getId() + "_catBanner", image));
        }
        else {
            if (category.getBannerImage() != null) {
                fileUploadUtilService.handleMediaDeletion(category.getBannerImage());
            }
        }

        return new CategoriesDTO(categoriesRepository.save(category));
    }

    @Transactional
    @CacheEvict(value = {"categoriesCache", "collectionsCache", "itemsCache"}, allEntries = true)
    public String delete(String slug) {

        Categories category = categoriesRepository.findBySlug(slug)
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