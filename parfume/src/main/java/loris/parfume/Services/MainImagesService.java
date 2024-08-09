package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.Models.MainImages;
import loris.parfume.Repositories.MainImagesRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MainImagesService {

    private final MainImagesRepository mainImagesRepository;
    private final FileUploadUtilService fileUploadUtilService;

    @Value("${pageSize}")
    private Integer pageSize;

    @Transactional
    @CacheEvict(value = "mainImagesCache", allEntries = true)
    public MainImages create(MultipartFile image, String name) {

        MainImages mainImages = new MainImages();

        mainImages.setCreatedTime(LocalDateTime.now());
        mainImages.setName(name);

        mainImagesRepository.save(mainImages);

        mainImages.setImageName(fileUploadUtilService.handleMediaUpload(mainImages.getId() + "_mainImage", image));

        return mainImagesRepository.save(mainImages);
    }

    @Cacheable(
            value = "mainImagesCache",
            key = "T(String).valueOf('page-').concat(T(String).valueOf(#page))"
    )
    public Page<MainImages> all(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return mainImagesRepository.findAll(pageable);
    }

    public MainImages getById(Long id) {

        return mainImagesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Image Not Found"));
    }

    @Transactional
    @CacheEvict(value = "mainImagesCache", allEntries = true)
    public MainImages update(Long id, MultipartFile image, String name) {

        MainImages mainImages = getById(id);

        fileUploadUtilService.handleMediaDeletion(mainImages.getImageName());

        Optional.ofNullable(name).ifPresent(mainImages::setName);

        if (image != null && !image.isEmpty()) {

            mainImages.setImageName(fileUploadUtilService.handleMediaUpload(mainImages.getId() + "_mainImage", image));
        }

        return mainImagesRepository.save(mainImages);
    }

    @CacheEvict(value = "mainImagesCache", allEntries = true)
    public String delete(Long id) {

        MainImages mainImages = getById(id);

        fileUploadUtilService.handleMediaDeletion(mainImages.getImageName());

        mainImagesRepository.delete(mainImages);

        return "Successfully Deleted";
    }
}