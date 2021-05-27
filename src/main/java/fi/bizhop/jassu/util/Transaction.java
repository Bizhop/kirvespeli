package fi.bizhop.jassu.util;

import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fi.bizhop.jassu.exception.TransactionException.Type.*;

public class Transaction {
    private static final Logger LOG = LogManager.getLogger(Transaction.class);
    private static final long TX_TIMEOUT = 5 * 1000; //transaction timeout (5s)

    private User lockUser = null;
    private String rollbackState = null;
    private long startTime = 0;

    public synchronized void begin(User user, String rollbackState) throws TransactionException {
        if(this.lockUser != null) {
            if(this.startTime == 0) throw new TransactionException(INTERNAL, "No startTime set on transaction");
            if(System.currentTimeMillis() > this.startTime + TX_TIMEOUT) {
                LOG.warn(String.format("Transaction has timed out for user: %s", this.lockUser.getEmail()));
                throw new TransactionException(TIMEOUT);
            }
            else {
                throw new TransactionException(LOCK, String.format("Transaction is locked for %s", this.lockUser.getEmail()));
            }
        }
        this.lockUser = user;
        this.rollbackState = rollbackState;
        this.startTime = System.currentTimeMillis();
    }

    public synchronized void check(User user) throws TransactionException {
        if(this.startTime == 0) throw new TransactionException(INTERNAL, "No startTime set on transaction");
        if(this.lockUser == null) throw new TransactionException(LOCK, "No lock when checking");
        if(System.currentTimeMillis() > this.startTime + TX_TIMEOUT) throw new TransactionException(TIMEOUT, String.format("Transaction has timed out for user: %s", this.lockUser.getEmail()));
        if(!this.lockUser.equals(user)) throw new TransactionException(LOCK, "You don't have lock");
    }

    public synchronized <T> T rollback(Class<T> type) throws TransactionException {
        if(this.rollbackState == null) throw new TransactionException(INTERNAL, "Unable to rollback, rollbackState is null");
        LOG.info(String.format("Perform rollback. User with lock was %s", this.lockUser.getEmail()));
        this.lockUser = null;
        T response = JsonUtil.getJavaObject(this.rollbackState, type)
                .orElseThrow(() -> new TransactionException(INTERNAL, String.format("Unable to convert rollbackState to %s", type.getSimpleName())));
        this.rollbackState = null;
        this.startTime = 0;
        return response;
    }

    public synchronized void end() throws TransactionException {
        if(this.startTime == 0) throw new TransactionException(INTERNAL, "No startTime set on transaction");
        if(this.lockUser == null) throw new TransactionException(LOCK, "No lock when ending");
        if(System.currentTimeMillis() > this.startTime + TX_TIMEOUT) throw new TransactionException(TIMEOUT, String.format("Transaction has timed out for user: %s", this.lockUser.getEmail()));
        this.lockUser = null;
        this.rollbackState = null;
        this.startTime = 0;
    }
}
