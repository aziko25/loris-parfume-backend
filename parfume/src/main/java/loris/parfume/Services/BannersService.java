package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Filters.BannerFilters;
import loris.parfume.DTOs.Requests.BannersRequest;
import loris.parfume.Models.Banners;
import loris.parfume.Repositories.BannersRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BannersService {

    private final BannersRepository bannersRepository;
    private final CacheForAllService cacheForAllService;

    private final FileUploadUtilService fileUploadUtilService;

    @Transactional
    @CacheEvict(value = "bannersCache", allEntries = true)
    public Banners create(List<MultipartFile> images, BannersRequest bannersRequest) {

        Banners banner = Banners.builder()
                .createdTime(LocalDateTime.now())
                .titleUz(bannersRequest.getTitleUz())
                .titleRu(bannersRequest.getTitleRu())
                .titleEng(bannersRequest.getTitleEng())
                .redirectTo(bannersRequest.getRedirectTo())
                .isActive(bannersRequest.getIsActive())
                .build();

        bannersRepository.save(banner);

        updateImages(images, banner);

        return bannersRepository.save(banner);
    }

    public List<Banners> all(BannerFilters filters) {

        if (filters == null) {

            return cacheForAllService.allBanners();
        }

        return bannersRepository.findAllByTitleLikeIgnoreCaseOrIsActive(filters.getTitle(), filters.getIsActive());
    }

    public Banners getById(Long id) {

        return bannersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Banner Not Found"));
    }

    @CacheEvict(value = "bannersCache", allEntries = true)
    public Banners update(Long id, List<MultipartFile> images, BannersRequest bannersRequest) {

        Banners banner = getById(id);

        if (images != null && !images.isEmpty()) {

            deleteImages(banner);
            updateImages(images, banner);
        }
        else {

            deleteImages(banner);
        }

        Optional.ofNullable(bannersRequest.getRedirectTo()).ifPresent(banner::setRedirectTo);
        Optional.ofNullable(bannersRequest.getTitleUz()).ifPresent(banner::setTitleUz);
        Optional.ofNullable(bannersRequest.getTitleRu()).ifPresent(banner::setTitleRu);
        Optional.ofNullable(bannersRequest.getTitleEng()).ifPresent(banner::setTitleEng);
        Optional.ofNullable(bannersRequest.getIsActive()).ifPresent(banner::setIsActive);

        return bannersRepository.save(banner);
    }

    @CacheEvict(value = "bannersCache", allEntries = true)
    public String delete(Long id) {

        Banners banner = getById(id);

        deleteImages(banner);

        bannersRepository.delete(banner);

        return "Banner Deleted Successfully";
    }

    private void updateImages(List<MultipartFile> images, Banners banner) {

        String[] languages = {"uz", "ru", "eng"};

        for (int i = 0; i < images.size(); i++) {

            String imageName = fileUploadUtilService.handleMediaUpload(
                    banner.getId() + "_" + (i + 1) + "_banner", images.get(i));

            switch (languages[i]) {

                case "uz":
                    banner.setImageNameUz(imageName);
                    break;

                case "ru":
                    banner.setImageNameRu(imageName);
                    break;

                case "eng":
                    banner.setImageNameEng(imageName);
                    break;
            }
        }
    }

    private void deleteImages(Banners banner) {

        List<String> imagesList = new ArrayList<>();

        imagesList.add(banner.getImageNameUz());
        imagesList.add(banner.getImageNameRu());
        imagesList.add(banner.getImageNameEng());

        fileUploadUtilService.handleMultipleMediaDeletion(imagesList);

        banner.setImageNameUz(null);
        banner.setImageNameRu(null);
        banner.setImageNameEng(null);

        bannersRepository.save(banner);
    }
}