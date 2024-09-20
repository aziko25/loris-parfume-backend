package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Requests.CataloguesRequest;
import loris.parfume.Models.Catalogues;
import loris.parfume.Repositories.CataloguesRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CataloguesService {

    private final CataloguesRepository cataloguesRepository;

    private final FileUploadUtilService fileUploadUtilService;

    @Transactional
    @CacheEvict(value = "cataloguesCache", allEntries = true)
    public Catalogues create(CataloguesRequest cataloguesRequest, List<MultipartFile> files) throws IOException {

        Catalogues catalogues = Catalogues.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(cataloguesRequest.getNameUz())
                .nameRu(cataloguesRequest.getNameRu())
                .nameEng(cataloguesRequest.getNameEng())
                .descriptionUz(cataloguesRequest.getDescriptionUz())
                .descriptionRu(cataloguesRequest.getDescriptionRu())
                .descriptionEng(cataloguesRequest.getDescriptionEng())
                .build();

        cataloguesRepository.save(catalogues);

        updateFiles(files, catalogues);

        return cataloguesRepository.save(catalogues);
    }

    @Cacheable(
            value = "cataloguesCache",
            key = "'allCatalogues'"
    )
    public List<Catalogues> all() {

        return cataloguesRepository.findAll(Sort.by("createdTime").descending());
    }

    public Catalogues getById(Long id) {

        return cataloguesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Catalogue Not Found"));
    }

    @Transactional
    @CacheEvict(value = "cataloguesCache", allEntries = true)
    public Catalogues update(Long id, CataloguesRequest cataloguesRequest, List<MultipartFile> files) throws IOException {

        Catalogues catalogues = getById(id);

        Optional.ofNullable(cataloguesRequest.getNameUz()).ifPresent(catalogues::setNameUz);
        Optional.ofNullable(cataloguesRequest.getNameRu()).ifPresent(catalogues::setNameRu);
        Optional.ofNullable(cataloguesRequest.getNameEng()).ifPresent(catalogues::setNameEng);

        Optional.ofNullable(cataloguesRequest.getDescriptionUz()).ifPresent(catalogues::setDescriptionUz);
        Optional.ofNullable(cataloguesRequest.getDescriptionRu()).ifPresent(catalogues::setDescriptionRu);
        Optional.ofNullable(cataloguesRequest.getDescriptionEng()).ifPresent(catalogues::setDescriptionEng);

        List<String> fileNames = new ArrayList<>();

        fileNames.add(catalogues.getFileUz());
        fileNames.add(catalogues.getFileRu());
        fileNames.add(catalogues.getFileEng());

        fileUploadUtilService.handleMultipleMediaDeletion(fileNames);

        if (files != null && !files.isEmpty()) {

            updateFiles(files, catalogues);
        }

        return cataloguesRepository.save(catalogues);
    }

    @Transactional
    @CacheEvict(value = "cataloguesCache", allEntries = true)
    public String delete(Long id) {

        Catalogues catalogues = getById(id);

        List<String> fileNames = new ArrayList<>();

        fileNames.add(catalogues.getFileUz());
        fileNames.add(catalogues.getFileRu());
        fileNames.add(catalogues.getFileEng());

        fileUploadUtilService.handleMultipleMediaDeletion(fileNames);

        cataloguesRepository.delete(catalogues);

        return "Catalogue Deleted";
    }

    private void updateFiles(List<MultipartFile> files, Catalogues catalogue) throws IOException {

        String[] languages = {"uz", "ru", "eng"};

        for (int i = 0; i < files.size(); i++) {

            String fileName = fileUploadUtilService.handleMediaUpload(files.get(i));

            switch (languages[i]) {

                case "uz":
                    catalogue.setFileUz(fileName);
                    break;

                case "ru":
                    catalogue.setFileRu(fileName);
                    break;

                case "eng":
                    catalogue.setFileEng(fileName);
                    break;
            }
        }
    }
}