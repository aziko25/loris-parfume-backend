package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Filters.BannerFilters;
import loris.parfume.DTOs.Requests.CollectionBannersRequest;
import loris.parfume.Models.CollectionBanners;
import loris.parfume.Repositories.CollectionBannersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollectionBannersService {

    private final CollectionBannersRepository collectionBannersRepository;
    private final CacheForAllService cacheForAllService;

    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @Transactional
    @CacheEvict(value = "collectionsBannersCache", allEntries = true)
    public CollectionBanners create(CollectionBannersRequest collectionBannersRequest) throws IOException {

        CollectionBanners collectionBanner = CollectionBanners.builder()
                .createdTime(LocalDateTime.now())
                .titleUz(collectionBannersRequest.getTitleUz())
                .titleRu(collectionBannersRequest.getTitleRu())
                .titleEng(collectionBannersRequest.getTitleEng())
                .redirectTo(collectionBannersRequest.getRedirectTo())
                .isActive(collectionBannersRequest.getIsActive())
                .build();

        collectionBannersRepository.save(collectionBanner);

        updateImages(collectionBannersRequest.getImagesUrl(), collectionBanner);

        return collectionBannersRepository.save(collectionBanner);
    }

    public Page<CollectionBanners> all(BannerFilters filters, Integer page) {

        if (filters == null) {

            return cacheForAllService.allCollectionsBanners(page);
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").ascending());

        return collectionBannersRepository.findAllByTitleLikeIgnoreCaseOrIsActive(filters.getTitle(), filters.getIsActive(), pageable);
    }

    public CollectionBanners getById(Long id) {

        return collectionBannersRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Collection Banner Not Found"));
    }

    @CacheEvict(value = "collectionsBannersCache", allEntries = true)
    public CollectionBanners update(Long id, CollectionBannersRequest collectionBannersRequest) throws IOException {

        CollectionBanners collectionBanner = getById(id);

        if (collectionBannersRequest.getImagesUrl() != null && !collectionBannersRequest.getImagesUrl().isEmpty()) {

            deleteImages(collectionBanner);
            updateImages(collectionBannersRequest.getImagesUrl(), collectionBanner);
        }
        else {

            deleteImages(collectionBanner);
        }

        Optional.ofNullable(collectionBannersRequest.getRedirectTo()).ifPresent(collectionBanner::setRedirectTo);
        Optional.ofNullable(collectionBannersRequest.getTitleUz()).ifPresent(collectionBanner::setTitleUz);
        Optional.ofNullable(collectionBannersRequest.getTitleRu()).ifPresent(collectionBanner::setTitleRu);
        Optional.ofNullable(collectionBannersRequest.getTitleEng()).ifPresent(collectionBanner::setTitleEng);
        Optional.ofNullable(collectionBannersRequest.getIsActive()).ifPresent(collectionBanner::setIsActive);

        return collectionBannersRepository.save(collectionBanner);
    }

    @CacheEvict(value = "collectionsBannersCache", allEntries = true)
    public String delete(Long id) {

        CollectionBanners collectionBanner = getById(id);

        deleteImages(collectionBanner);

        collectionBannersRepository.delete(collectionBanner);

        return "Collection Banner Deleted Successfully";
    }

    private void updateImages(List<String> images, CollectionBanners collectionBanner) {

        deleteImages(collectionBanner);

        String[] languages = {"uz", "ru", "eng"};

        for (int i = 0; i < images.size(); i++) {

            String imageName = images.get(i);

            switch (languages[i]) {

                case "uz":
                    collectionBanner.setImageNameUz(imageName);
                    break;

                case "ru":
                    collectionBanner.setImageNameRu(imageName);
                    break;

                case "eng":
                    collectionBanner.setImageNameEng(imageName);
                    break;
            }
        }
    }

    private void deleteImages(CollectionBanners collectionBanner) {

        List<String> imagesList = new ArrayList<>();

        imagesList.add(collectionBanner.getImageNameUz());
        imagesList.add(collectionBanner.getImageNameRu());
        imagesList.add(collectionBanner.getImageNameEng());

        fileUploadUtilService.handleMultipleMediaDeletion(imagesList);

        collectionBanner.setImageNameUz(null);
        collectionBanner.setImageNameRu(null);
        collectionBanner.setImageNameEng(null);

        collectionBannersRepository.save(collectionBanner);
    }
}