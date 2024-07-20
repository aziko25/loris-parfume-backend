package loris.parfume.Services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.DTOs.Requests.UsersRequest;
import loris.parfume.DTOs.returnDTOs.UsersDTO;
import loris.parfume.Models.Users;
import loris.parfume.Repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static loris.parfume.Configurations.JWT.AuthorizationMethods.USER_ID;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;

    @Value("${pageSize}")
    private Integer pageSize;

    public Page<UsersDTO> all(Integer page) {

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("registrationTime").descending());

        return usersRepository.findAll(pageable).map(UsersDTO::new);
    }

    public UsersDTO me() {

        return usersRepository.findById(USER_ID).map(UsersDTO::new)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));
    }

    public UsersDTO update(UsersRequest usersRequest) {

        Users user = usersRepository.findById(USER_ID).orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        if (usersRequest.getPhone() != null) {

            Users existsByPhone = usersRepository.findByPhone(usersRequest.getPhone());

            if (existsByPhone != null && !existsByPhone.getId().equals(user.getId())) {

                throw new EntityExistsException("This Phone Already Exist!");
            }

            user.setPhone(usersRequest.getPhone());
        }

        Optional.ofNullable(usersRequest.getFullName()).ifPresent(user::setFullName);

        if (usersRequest.getPassword() != null) {

            if (!usersRequest.getPassword().equals(usersRequest.getRePassword())) {

                throw new IllegalArgumentException("Passwords Do Not Match!");
            }

            user.setPassword(usersRequest.getPassword());
        }

        return new UsersDTO(usersRepository.save(user));
    }
}