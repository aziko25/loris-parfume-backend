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

import java.io.IOException;
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
    public Banners create(BannersRequest bannersRequest) throws IOException {

        Banners banner = Banners.builder()
                .createdTime(LocalDateTime.now())
                .titleUz(bannersRequest.getTitleUz())
                .titleRu(bannersRequest.getTitleRu())
                .titleEng(bannersRequest.getTitleEng())
                .redirectTo(bannersRequest.getRedirectTo())
                .isActive(bannersRequest.getIsActive())
                .build();

        bannersRepository.save(banner);

        if (bannersRequest.getDesktopImagesUrl() != null && !bannersRequest.getDesktopImagesUrl().isEmpty()) {
            updateImages(bannersRequest.getDesktopImagesUrl(), banner, "desktop");
        }

        if (bannersRequest.getMobileImagesUrl() != null && !bannersRequest.getMobileImagesUrl().isEmpty()) {
            updateImages(bannersRequest.getMobileImagesUrl(), banner, "mobile");
        }

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
    public Banners update(Long id, BannersRequest bannersRequest) throws IOException {

        Banners banner = getById(id);

        if (bannersRequest.getDesktopImagesUrl() != null && !bannersRequest.getDesktopImagesUrl().isEmpty()) {

            deleteImages(banner, "desktop");
            updateImages(bannersRequest.getDesktopImagesUrl(), banner, "desktop");
        }
        else {

            deleteImages(banner, "desktop");
        }

        if (bannersRequest.getMobileImagesUrl() != null && !bannersRequest.getMobileImagesUrl().isEmpty()) {

            deleteImages(banner, "mobile");
            updateImages(bannersRequest.getMobileImagesUrl(), banner, "mobile");
        }
        else {

            deleteImages(banner, "mobile");
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

        deleteImages(banner, "desktop");
        deleteImages(banner, "mobile");

        bannersRepository.delete(banner);

        return "Banner Deleted Successfully";
    }

    private void updateImages(List<String> images, Banners banner, String type) {

        String[] languages = {"uz", "ru", "eng"};

        for (int i = 0; i < images.size(); i++) {

            String imageName = images.get(i);

            switch (languages[i]) {

                case "uz":

                    if (type.equals("desktop")) {
                        banner.setDesktopImageNameUz(imageName);
                    }
                    else {
                        banner.setMobileImageNameUz(imageName);
                    }

                    break;

                case "ru":

                    if (type.equals("desktop")) {
                        banner.setDesktopImageNameRu(imageName);
                    }
                    else {
                        banner.setMobileImageNameRu(imageName);
                    }

                    break;

                case "eng":

                    if (type.equals("desktop")) {
                        banner.setDesktopImageNameEng(imageName);
                    }
                    else {
                        banner.setMobileImageNameEng(imageName);
                    }

                    break;
            }
        }
    }

    private void deleteImages(Banners banner, String type) {

        List<String> imagesList = new ArrayList<>();

        if (type.equals("desktop")) {
            imagesList.add(banner.getDesktopImageNameUz());
            imagesList.add(banner.getDesktopImageNameRu());
            imagesList.add(banner.getDesktopImageNameEng());
        } else {
            imagesList.add(banner.getMobileImageNameUz());
            imagesList.add(banner.getMobileImageNameRu());
            imagesList.add(banner.getMobileImageNameEng());
        }

        fileUploadUtilService.handleMultipleMediaDeletion(imagesList);

        if (type.equals("desktop")) {
            banner.setDesktopImageNameUz(null);
            banner.setDesktopImageNameRu(null);
            banner.setDesktopImageNameEng(null);
        } else {
            banner.setMobileImageNameUz(null);
            banner.setMobileImageNameRu(null);
            banner.setMobileImageNameEng(null);
        }

        bannersRepository.save(banner);
    }
}