package fi.bizhop.jassu.util;

import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static fi.bizhop.jassu.exception.TransactionException.Type.INTERNAL;

public class TransactionHandler {
    private static final Logger LOG = LogManager.getLogger(TransactionHandler.class);

    private final Map<Long, Transaction> TXS = new ConcurrentHashMap<>();

    public TransactionHandler() {}

    public synchronized void registerGame(Long id) throws TransactionException {
        if(this.TXS.containsKey(id)) throw new TransactionException(INTERNAL, String.format("Game id=%d already registered to TransactionHandler", id));

        this.TXS.put(id, new Transaction());
        LOG.info(String.format("Transaction handler registered for game id=%d", id));
    }

    public synchronized void begin(Long id, User user, String rollbackState) throws TransactionException {
        if(!this.TXS.containsKey(id)) throw new TransactionException(INTERNAL, "Game must be registered to TransactionHandler");

        var tx = this.TXS.get(id);
        tx.begin(user, rollbackState);
        tx.check(user);
    }

    public synchronized <T> T rollback(Long id, Class<T> type) throws TransactionException {
        if(!this.TXS.containsKey(id)) throw new TransactionException(INTERNAL, "Game must be registered to TransactionHandler");

        return this.TXS.get(id).rollback(type);
    }

    public synchronized void end(Long id) throws TransactionException {
        if(!this.TXS.containsKey(id)) throw new TransactionException(INTERNAL, "Game must be registered to TransactionHandler");

        this.TXS.get(id).end();
    }
}
