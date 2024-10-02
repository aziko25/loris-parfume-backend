package loris.parfume.Repositories;

import loris.parfume.Models.Blog;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogsRepository extends JpaRepository<Blog, Long> {

    List<Blog> findAllByIsActiveIsTrue(Sort createdTime);
}