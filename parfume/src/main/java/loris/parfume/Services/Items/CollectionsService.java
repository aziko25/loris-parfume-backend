package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Items.CollectionsRequest;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Collections_Items;
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.Collections_Items_Repository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionsService {

    private final CollectionsRepository collectionsRepository;
    private final CategoriesRepository categoriesRepository;
    private final Collections_Items_Repository collectionsItemsRepository;
    private final Orders_Items_Repository ordersItemsRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public Collections create(CollectionsRequest collectionsRequest) {

        Collections collection = Collections.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(collectionsRequest.getNameUz())
                .nameRu(collectionsRequest.getNameRu())
                .nameEng(collectionsRequest.getNameEng())
                .build();

        return collectionsRepository.save(collection);
    }

    public Page<Collections> all(Integer page, String name) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("nameUz"));

        if (name != null) {

            return collectionsRepository.findAllByAnyNameLikeIgnoreCase("%" + name + "%", pageable);
        }

        return collectionsRepository.findAll(pageable);
    }

    public Collections getById(Long id) {

        return collectionsRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));
    }

    public Collections update(Long id, CollectionsRequest collectionsRequest) {

        Collections collection = getById(id);

        if (collectionsRequest != null) {

            Optional.ofNullable(collectionsRequest.getNameUz()).ifPresent(collection::setNameUz);
            Optional.ofNullable(collectionsRequest.getNameRu()).ifPresent(collection::setNameRu);
            Optional.ofNullable(collectionsRequest.getNameEng()).ifPresent(collection::setNameEng);
        }

        return collectionsRepository.save(collection);
    }

    @Transactional
    public String delete(Long id) {

        Collections collection = getById(id);

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