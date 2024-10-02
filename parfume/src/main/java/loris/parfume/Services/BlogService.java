package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.Configurations.Images.FileUploadUtilService;
import loris.parfume.DTOs.Requests.BlogsRequest;
import loris.parfume.Models.Blog;
import loris.parfume.Repositories.BlogsRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogsRepository blogsRepository;
    private final FileUploadUtilService fileUploadUtilService;

    public Blog create(MultipartFile image, BlogsRequest blogsRequest) throws IOException {

        Blog blog = Blog.builder()
                .createdTime(LocalDateTime.now())
                .titleUz(blogsRequest.getTitleUz())
                .titleRu(blogsRequest.getTitleRu())
                .descriptionUz(blogsRequest.getDescriptionUz())
                .descriptionRu(blogsRequest.getDescriptionRu())
                .mainImage(fileUploadUtilService.handleMediaUpload(image))
                .isActive(blogsRequest.getIsActive())
                .build();

        return blogsRepository.save(blog);
    }


    public List<Blog> all(Boolean isActive) {

        if (isActive == null) {

            return blogsRepository.findAll(Sort.by("createdTime").descending());
        }

        return blogsRepository.findAllByIsActiveIsTrue(Sort.by("createdTime").descending());
    }

    public Blog getById(Long blogId) {

        return blogsRepository.findById(blogId).orElseThrow(() -> new EntityNotFoundException("Blog Not Found"));
    }

    public String delete(Long blogId) {

        blogsRepository.deleteById(blogId);

        return "Blog Successfully Deleted";
    }
}