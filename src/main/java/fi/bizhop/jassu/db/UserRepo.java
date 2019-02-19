package fi.bizhop.jassu.db;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepo extends CrudRepository<UserDB, Long> {
    Optional<UserDB> findByEmail(String email);
}
