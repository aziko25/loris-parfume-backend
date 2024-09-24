package loris.parfume.Services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import loris.parfume.Models.B2B_Users;
import loris.parfume.Repositories.B2B_Users_Repository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class B2B_Users_Service {

    private final B2B_Users_Repository b2BUsersRepository;

    public B2B_Users create(B2B_Users b2BUser) {

        B2B_Users bUSer = B2B_Users.builder()
                .createdTime(LocalDateTime.now())
                .fullName(b2BUser.getFullName())
                .email(b2BUser.getEmail())
                .phone(b2BUser.getPhone())
                .contactSource(b2BUser.getContactSource())
                .message(b2BUser.getMessage())
                .build();

        return b2BUsersRepository.save(bUSer);
    }

    public List<B2B_Users> all() {

        return b2BUsersRepository.findAll(Sort.by("createdTime").descending());
    }

    public B2B_Users getById(Long id) {

        return b2BUsersRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("B2B User Not Found"));
    }

    public String delete(Long id) {

        B2B_Users bUSer = getById(id);

        b2BUsersRepository.delete(bUSer);

        return "Successfully Deleted";
    }
}