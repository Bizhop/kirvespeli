package fi.bizhop.jassu.db;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface KirvesGameRepo extends CrudRepository<KirvesGameDB, Long> {
    List<KirvesGameDB> findByActiveTrue();
    Optional<KirvesGameDB> findByIdAndActiveTrue(Long id);
}
