package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.WishlistDTO;
import loris.parfume.Models.Items.Collections;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Items.Sizes;
import loris.parfume.Models.Items.Sizes_Items;
import loris.parfume.Models.Users;
import loris.parfume.Models.Wishlist;
import loris.parfume.Repositories.Items.CollectionsRepository;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.Items.SizesRepository;
import loris.parfume.Repositories.Items.Sizes_Items_Repository;
import loris.parfume.Repositories.UsersRepository;
import loris.parfume.Repositories.WishlistRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;
import static loris.parfume.DefaultEntitiesService.DEFAULT_NO_SIZE;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;

    private final CollectionsRepository collectionsRepository;
    private final SizesRepository sizesRepository;
    private final Sizes_Items_Repository sizesItemsRepository;
    private final UsersRepository usersRepository;
    private final ItemsRepository itemsRepository;

    public WishlistDTO add(Long itemId, Long collectionId, Long sizeId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));
        Collections collection = collectionsRepository.findById(collectionId).orElseThrow(() -> new EntityNotFoundException("Collection Not Found"));
        Sizes size;

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .item(item)
                .collection(collection)
                .addedTime(LocalDateTime.now())
                .build();

        if (sizeId != null) {

            size = sizesRepository.findById(sizeId)
                    .orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(item, size);

            if (sizesItem == null) {

                throw new EntityNotFoundException("Item With This Size Not Found");
            }

            wishlist.setSize(size);
            wishlistRepository.save(wishlist);

            return new WishlistDTO(sizesItem, collection);
        }
        else {

            if (!item.getSizesItemsList().isEmpty()) {

                throw new EntityNotFoundException("Select Item's Size!");
            }

            size = sizesRepository.findById(DEFAULT_NO_SIZE)
                    .orElseThrow(() -> new EntityNotFoundException("Default Size Not Found"));

            wishlist.setSize(size);
            wishlistRepository.save(wishlist);

            Sizes_Items sizesItem = Sizes_Items.builder()
                    .item(item)
                    .size(size)
                    .price(item.getPrice())
                    .discountPercent(item.getDiscountPercent())
                    .build();

            return new WishlistDTO(sizesItem, collection);
        }
    }

    public List<WishlistDTO> all() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<Wishlist> wishlistList = wishlistRepository.findAllByUser(user, Sort.by("addedTime").descending());
        List<WishlistDTO> wishlistDTOList = new ArrayList<>();

        for (Wishlist wishlist : wishlistList) {

            Sizes_Items sizesItem = sizesItemsRepository.findByItemAndSize(wishlist.getItem(), wishlist.getSize());

            wishlistDTOList.add(new WishlistDTO(sizesItem, wishlist.getCollection()));
        }

        return wishlistDTOList;
    }

    @Transactional
    public String remove(Long itemId, Long sizeId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        if (sizeId != null) {

            Sizes size = sizesRepository.findById(sizeId).orElseThrow(() -> new EntityNotFoundException("Size Not Found"));

            wishlistRepository.deleteByUserAndItemAndSize(user, item, size);
        }
        else {

            wishlistRepository.deleteByUserAndItem(user, item);
        }

        return "Successfully Deleted";
    }

    @Transactional
    public String clear() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        wishlistRepository.deleteAllByUser(user);

        return "Successfully Cleared The Cart";
    }
}