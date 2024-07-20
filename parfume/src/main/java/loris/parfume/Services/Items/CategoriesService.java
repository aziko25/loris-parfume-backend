package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Filters.CategoryFilters;
import loris.parfume.DTOs.Requests.Items.CategoriesRequest;
import loris.parfume.DTOs.returnDTOs.CategoriesDTO;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final CollectionsRepository collectionsRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public CategoriesDTO create(CategoriesRequest categoriesRequest) {

        Collections collection = collectionsRepository.findById(categoriesRequest.getCollectionId())
                .orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));

        Categories category = Categories.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(categoriesRequest.getNameUz())
                .nameRu(categoriesRequest.getNameRu())
                .nameEng(categoriesRequest.getNameEng())
                .collection(collection)
                .build();

        return new CategoriesDTO(categoriesRepository.save(category));
    }

    public Page<CategoriesDTO> all(Integer page, CategoryFilters filters) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz"));

        if (filters != null) {

            return categoriesRepository.findAllByFilters(filters.getName(), filters.getCollectionId(), pageable).map(CategoriesDTO::new);
        }

        return categoriesRepository.findAll(pageable).map(CategoriesDTO::new);
    }

    public CategoriesDTO getById(Long id) {

        return categoriesRepository.findById(id).map(CategoriesDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));
    }

    public CategoriesDTO update(Long id, CategoriesRequest categoriesRequest) {

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

        return new CategoriesDTO(categoriesRepository.save(category));
    }

    public String delete(Long id) {

        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category Not Found"));

        categoriesRepository.delete(category);

        return "Category Successfully Deleted";
    }
}