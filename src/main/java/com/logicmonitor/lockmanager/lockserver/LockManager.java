package com.logicmonitor.lockmanager.lockserver;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by kai on 16/5/14.
 * This is the class to perform the real lock operations.
 * Because this is a toy project, we don't provide any configuration about the lock manager
 */
public class LockManager {

    public static final int _LOCK_EXPIRATION_TIME_SECOND = 30;
    private static final int _MAX_LOCK_NUMBER = 1000000;
    private static final int _LOCK_WAITING_TIME_MS = 10;

    private static LockManager _instance = new LockManager();

    public static LockManager getInstance() {
        return _instance;
    }

    private Cache<String/*lock name*/, String/*session ID*/> _lockCache;
    private Object syncObjForAddLock;

    private LockManager() {
        _lockCache = CacheBuilder.newBuilder()
                .expireAfterWrite(_LOCK_EXPIRATION_TIME_SECOND, TimeUnit.SECONDS)
                .maximumSize(_MAX_LOCK_NUMBER)
                .build();
        syncObjForAddLock = new Object();
    }

    public String getLock(String lockName) {
        while (true) {
            try {
                return tryGetLock(lockName);
            }
            catch (LockServerException e) {
                try {
                    Thread.sleep(_LOCK_WAITING_TIME_MS);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public String tryGetLock(String lockName) throws LockServerException {
        String sessionID = _lockCache.getIfPresent(lockName);
        if (sessionID != null) {
            throw new LockServerException(String.format("Lock Exists, lockname=%s", lockName));
        }
        sessionID = UUID.randomUUID().toString();
        return _tryGetLockInternal(lockName, sessionID);
    }

    synchronized private String _tryGetLockInternal(String lockName, String sessionID) throws LockServerException {
        String cachedSessionID = _lockCache.getIfPresent(lockName);
        if (cachedSessionID != null) {
            throw new LockServerException(String.format("Lock Exists, lockname=%s", lockName));
        }
        synchronized (syncObjForAddLock) {
            _lockCache.put(lockName, sessionID);
        }
        return sessionID;
    }

    //Actually, sessionID should also be provided
    public void unLock(String lockName) {
        String sessionID = _lockCache.getIfPresent(lockName);
        if (sessionID == null) {
            return;
        }
        _unlockInternal(lockName);
    }

    synchronized private void _unlockInternal(String lockName) {
        String sessionID = _lockCache.getIfPresent(lockName);
        if (sessionID == null) {
            return;
        }
        _lockCache.asMap().remove(lockName);
    }

    public void heartbeat(String lockName, String sessionID) throws LockServerException {
        String cachedSessionID = _lockCache.getIfPresent(lockName);
        if (cachedSessionID == null) {
            throw new LockServerException(String.format("Not Exist lock:%s", lockName));
        }
        if (!cachedSessionID.equalsIgnoreCase(sessionID)) {
            throw new LockServerException(String.format("Invalid sessionID:%s", sessionID));
        }
        _heartBeatInternal(lockName, sessionID);
    }

    synchronized private void _heartBeatInternal(String lockName, String sessionID) throws LockServerException {
        String cachedSessionID = _lockCache.getIfPresent(lockName);
        if (cachedSessionID == null) {
            throw new LockServerException(String.format("Not Exist lock:%s", lockName));
        }
        synchronized (syncObjForAddLock) {
            if (!cachedSessionID.equalsIgnoreCase(sessionID)) {
                throw new LockServerException(String.format("Invalid sessionID:%s", sessionID));
            }
            _lockCache.put(lockName, sessionID);
        }
    }

    public String getAllLock() {
        return _lockCache.asMap().toString();
    }
}
