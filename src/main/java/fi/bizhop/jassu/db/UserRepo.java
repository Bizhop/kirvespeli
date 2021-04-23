package fi.bizhop.jassu.db;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<UserDB, Long> {
    Optional<UserDB> findByEmail(String email);
    List<UserDB> findByEmailIn(List<String> emails);
}
