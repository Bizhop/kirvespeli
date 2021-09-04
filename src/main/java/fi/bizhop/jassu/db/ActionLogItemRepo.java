package fi.bizhop.jassu.db;

import org.springframework.data.repository.CrudRepository;

public interface ActionLogItemRepo extends CrudRepository<ActionLogItemDB, Long> {
    Long deleteByActionLog(ActionLogDB actionLogDB);
}
