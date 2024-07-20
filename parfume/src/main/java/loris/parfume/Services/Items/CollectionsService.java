package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Items.CollectionsRequest;
import loris.parfume.Models.Items.Collections;
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
public class CollectionsService {

    private final CollectionsRepository collectionsRepository;

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

    public String delete(Long id) {

        Collections collection = getById(id);

        collectionsRepository.delete(collection);

        return "Collection Deleted Successfully";
    }
}