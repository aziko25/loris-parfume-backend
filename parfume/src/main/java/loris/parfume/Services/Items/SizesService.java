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
import loris.parfume.Models.Orders.Orders_Items;
import loris.parfume.Repositories.BasketsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import loris.parfume.Repositories.Orders.Orders_Items_Repository;
import loris.parfume.Repositories.WishlistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
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
    private final Orders_Items_Repository ordersItemsRepository;
    private final BasketsRepository basketsRepository;
    private final WishlistRepository wishlistRepository;

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

    @Transactional
    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
    public SizesDTO addItems(Long sizeId, List<Sizes_Items_Request> sizesItemsRequest) {

        Sizes size = sizesRepository.findById(sizeId).orElseThrow(() -> new EntityNotFoundException("Size Not Found!"));

        return setItemsToSize(sizesItemsRequest, size);
    }

    @Transactional
    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
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

    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
    public SizesDTO update(Long id, SizesRequest sizesRequest) {

        Sizes size = sizesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

        Optional.ofNullable(sizesRequest.getNameUz()).ifPresent(size::setNameUz);
        Optional.ofNullable(sizesRequest.getNameRu()).ifPresent(size::setNameRu);
        Optional.ofNullable(sizesRequest.getNameEng()).ifPresent(size::setNameEng);

        return new SizesDTO(sizesRepository.save(size));
    }

    @Transactional
    @CacheEvict(value = {"itemsCache", "ordersCache"}, allEntries = true)
    public String delete(Long id) {

        Sizes size = sizesRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

        if (size.getIsDefaultNoSize()) {

            throw new IllegalArgumentException("You Can't Delete Default Size!");
        }

        Sizes defaultSize = sizesRepository.findByIsDefaultNoSize(true);

        sizesItemsRepository.deleteAllBySize(size);
        wishlistRepository.deleteAllBySize(size);
        basketsRepository.deleteAllBySize(size);

        List<Orders_Items> ordersItemsList = ordersItemsRepository.findAllBySize(size);

        List<Orders_Items> batchUpdateOrderItemsList = new ArrayList<>();
        for (Orders_Items ordersItem : ordersItemsList) {

            ordersItem.setSize(defaultSize);
            batchUpdateOrderItemsList.add(ordersItem);
        }

        ordersItemsRepository.saveAll(batchUpdateOrderItemsList);

        sizesRepository.delete(size);

        return "Size Deleted";
    }
}