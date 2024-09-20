package loris.parfume.Configurations.AmazonS3;

import lombok.RequiredArgsConstructor;
import loris.parfume.Models.Banners;
import loris.parfume.Models.Catalogues;
import loris.parfume.Models.CollectionBanners;
import loris.parfume.Models.Items.Categories;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items_Images;
import loris.parfume.Repositories.BannersRepository;
import loris.parfume.Repositories.CataloguesRepository;
import loris.parfume.Repositories.CollectionBannersRepository;
import loris.parfume.Repositories.Items.CategoriesRepository;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.Items_Images_Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRidOfUnnecessaryImages {

    String dir = "/var/www/images/loris";

    private final CategoriesRepository categoriesRepository;
    private final CollectionsRepository collectionsRepository;
    private final Items_Images_Repository itemsImagesRepository;
    private final BannersRepository bannersRepository;
    private final CataloguesRepository cataloguesRepository;
    private final CollectionBannersRepository collectionBannersRepository;

    @Bean
    public void method() {

        List<String> allExistingImages = new ArrayList<>();

        allExistingImages.addAll(categoriesRepository.findAll().stream().map(Categories::getBannerImage).toList());
        allExistingImages.addAll(collectionsRepository.findAll().stream().map(Collections::getBannerImage).toList());
        allExistingImages.addAll(itemsImagesRepository.findAll().stream().map(Items_Images::getImageName).toList());

        allExistingImages.addAll(bannersRepository.findAll().stream().map(Banners::getDesktopImageNameUz).toList());
        allExistingImages.addAll(bannersRepository.findAll().stream().map(Banners::getDesktopImageNameRu).toList());
        allExistingImages.addAll(bannersRepository.findAll().stream().map(Banners::getMobileImageNameUz).toList());
        allExistingImages.addAll(bannersRepository.findAll().stream().map(Banners::getMobileImageNameRu).toList());

        allExistingImages.addAll(cataloguesRepository.findAll().stream().map(Catalogues::getFileUz).toList());
        allExistingImages.addAll(cataloguesRepository.findAll().stream().map(Catalogues::getFileRu).toList());

        allExistingImages.addAll(collectionBannersRepository.findAll().stream().map(CollectionBanners::getImageNameUz).toList());
        allExistingImages.addAll(collectionBannersRepository.findAll().stream().map(CollectionBanners::getImageNameRu).toList());

        System.out.println(allExistingImages);

        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {

            for (File file : listOfFiles) {

                if (file.isFile()) {

                    String fileName = file.getName();

                    if (!allExistingImages.contains(fileName)) {

                        if (file.delete()) {
                            System.out.println("Deleted: " + fileName);
                        }
                        else {
                            System.out.println("Failed to delete: " + fileName);
                        }
                    }
                }
            }
        }
        else {

            System.out.println("Directory is empty or doesn't exist.");
        }
    }

    /* static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        context.register(GetRidOfUnnecessaryImages.class);

        context.refresh();

        GetRidOfUnnecessaryImages service = context.getBean(GetRidOfUnnecessaryImages.class);

        service.method();

        context.close();
    }*/
}