package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.returnDTOs.ItemsDTO;
import loris.parfume.Models.Items.Items;
import loris.parfume.Models.Users;
import loris.parfume.Models.Wishlist;
import loris.parfume.Repositories.Items.ItemsRepository;
import loris.parfume.Repositories.UsersRepository;
import loris.parfume.Repositories.WishlistRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UsersRepository usersRepository;
    private final ItemsRepository itemsRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public String add(Long itemId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        wishlistRepository.save(new Wishlist(user, item));

        return "Successfully Added To Wishlist";
    }

    public Page<ItemsDTO> all(Integer page) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return wishlistRepository.findAllByUser(user, pageable).map(wishlist -> new ItemsDTO(wishlist.getItem()));
    }

    @Transactional
    public String remove(Long itemId) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Items item = itemsRepository.findById(itemId).orElseThrow(() -> new EntityNotFoundException("Item Not Found"));

        wishlistRepository.deleteByUserAndItem(user, item);

        return "Successfully Deleted";
    }

    @Transactional
    public String clear() {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        wishlistRepository.deleteAllByUser(user);

        return "Successfully Cleared The Cart";
    }
}