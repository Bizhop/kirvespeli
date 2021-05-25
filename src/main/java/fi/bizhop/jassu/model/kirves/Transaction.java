package fi.bizhop.jassu.model.kirves;

import fi.bizhop.jassu.exception.TransactionException;
import fi.bizhop.jassu.model.User;
import fi.bizhop.jassu.model.kirves.pojo.GameDataPOJO;
import fi.bizhop.jassu.util.JsonUtil;
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
        if(lockUser != null) {
            if(startTime == 0) throw new TransactionException(INTERNAL);
            if(System.currentTimeMillis() > startTime + TX_TIMEOUT) {
                LOG.warn(String.format("Transaction has timed out for user: %s", lockUser.getEmail()));
                throw new TransactionException(TIMEOUT);
            }
            else {
                throw new TransactionException(LOCK, String.format("Transaction is locked for %s", lockUser.getEmail()));
            }
        }
        this.lockUser = user;
        this.rollbackState = rollbackState;
        this.startTime = System.currentTimeMillis();
    }

    public synchronized void check(User user) throws TransactionException {
        if(startTime == 0) throw new TransactionException(INTERNAL);
        if(this.lockUser == null) throw new TransactionException(LOCK, "No lock when checking");
        if(System.currentTimeMillis() > startTime + TX_TIMEOUT) throw new TransactionException(TIMEOUT, String.format("Transaction has timed out for user: %s", lockUser.getEmail()));
        if(!this.lockUser.equals(user)) throw new TransactionException(LOCK, "You don't have lock");
    }

    public synchronized GameDataPOJO rollback() throws TransactionException {
        LOG.info(String.format("Perform rollback. User with lock was %s", this.lockUser.getEmail()));
        this.lockUser = null;
        GameDataPOJO response = JsonUtil.getJavaObject(this.rollbackState, GameDataPOJO.class)
                .orElseThrow(() -> new TransactionException(INTERNAL, "Unable to convert rollbackState to GameDataPOJO"));
        this.rollbackState = null;
        this.startTime = 0;
        return response;
    }

    public synchronized void end() throws TransactionException {
        if(startTime == 0) throw new TransactionException(INTERNAL);
        if(this.lockUser == null) throw new TransactionException(LOCK, "No lock when ending");
        if(System.currentTimeMillis() > startTime + TX_TIMEOUT) throw new TransactionException(TIMEOUT, String.format("Transaction has timed out for user: %s", lockUser.getEmail()));
        this.lockUser = null;
        this.rollbackState = null;
        this.startTime = 0;
    }
}
