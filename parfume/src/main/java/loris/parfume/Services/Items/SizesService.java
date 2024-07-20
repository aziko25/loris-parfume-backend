package loris.parfume.Services.Items;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.Items.SizesRequest;
import loris.parfume.DTOs.Requests.Items.Sizes_Items_Request;
import loris.parfume.DTOs.returnDTOs.SizesDTO;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SizesService {

    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final ItemsRepository itemsRepository;

    public static Long DEFAULT_NO_SIZE;

    @Value("${pageSize}")
    private Integer pageSize;

    public SizesDTO create(SizesRequest sizesRequest) {

        Sizes size = Sizes.builder()
                .createdTime(LocalDateTime.now())
                .nameUz(sizesRequest.getNameUz())
                .nameRu(sizesRequest.getNameRu())
                .nameEng(sizesRequest.getNameEng())
                .isDefaultNoSize(false)
                .build();

        return new SizesDTO(sizesRepository.save(size));
    }

    // Creates default no size for items that doesn't have sizes, so it must not be deleted
    @Bean
    public void createDefault() {

        System.out.println("Running size default");

        Sizes size = sizesRepository.findByIsDefaultNoSize(true);

        if (size == null) {

            size = Sizes.builder()
                    .createdTime(LocalDateTime.now())
                    .nameUz("Razmeri Yo'q")
                    .nameRu("Нет Размеров")
                    .nameEng("No Sizes")
                    .isDefaultNoSize(true)
                    .build();

            sizesRepository.save(size);
        }

        DEFAULT_NO_SIZE = size.getId();
    }

    @Transactional
    public SizesDTO addItems(Long sizeId, List<Sizes_Items_Request> sizesItemsRequest) {

        Sizes size = sizesRepository.findById(sizeId).orElseThrow(() -> new EntityNotFoundException("Size Not Found!"));

        return setItemsToSize(sizesItemsRequest, size);
    }

    @Transactional
    public SizesDTO updateItems(Long sizeId, List<Sizes_Items_Request> sizesItemsRequest) {

        Sizes size = sizesRepository.findById(sizeId).orElseThrow(() -> new EntityNotFoundException("Size Not Found!"));

        sizesItemsRepository.deleteAllBySize(size);

        return setItemsToSize(sizesItemsRequest, size);
    }

    private SizesDTO setItemsToSize(List<Sizes_Items_Request> sizesItemsRequest, Sizes size) {

        List<Sizes_Items> saveSizeItemsList = new ArrayList<>();

        for (Sizes_Items_Request request : sizesItemsRequest) {

            Items item = itemsRepository.findById(request.getItemId())
                    .orElseThrow(() -> new EntityNotFoundException("Item " + request.getItemId() + " Not Found!"));

            Sizes_Items sizesItem = new Sizes_Items(size, item, request.getPrice(), request.getQuantity(), request.getDiscountPercent());

            saveSizeItemsList.add(sizesItem);
        }

        sizesItemsRepository.saveAll(saveSizeItemsList);

        size.setItemsList(saveSizeItemsList.stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                item -> item.getItem().getId(),
                                item -> item,
                                (existing, replacement) -> existing
                        ),
                        map -> new ArrayList<>(map.values())
                )));

        return new SizesDTO(size);
    }

    public Page<SizesDTO> all(Integer page, String search) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("isDefaultNoSize")));

        return sizesRepository.findAllByFilters(search, pageable).map(SizesDTO::new);
    }

    public SizesDTO getById(Long id) {

        return sizesRepository.findById(id)
                .map(SizesDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("Size Not Found"));
    }

    public SizesDTO update(Long id, SizesRequest sizesRequest) {

        Sizes size = sizesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

        Optional.ofNullable(sizesRequest.getNameUz()).ifPresent(size::setNameUz);
        Optional.ofNullable(sizesRequest.getNameRu()).ifPresent(size::setNameRu);
        Optional.ofNullable(sizesRequest.getNameEng()).ifPresent(size::setNameEng);

        return new SizesDTO(sizesRepository.save(size));
    }

    @Transactional
    public String delete(Long id) {

        Sizes size = sizesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

        sizesItemsRepository.deleteAllBySize(size);

        sizesRepository.delete(size);

        return "Size Deleted";
    }
}