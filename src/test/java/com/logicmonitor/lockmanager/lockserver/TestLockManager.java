package com.logicmonitor.lockmanager.lockserver;

import org.junit.Assert;
import org.junit.Test;


/**
 * Created by kai on 16/5/14.
 */
public class TestLockManager {

    private static LockManager _lockManager = LockManager.getInstance();

    @Test
    public void testGetLock() {
        String sessionID = _lockManager.getLock("TEST_GET_LOCK");
        Assert.assertNotNull(sessionID);
    }

    @Test
    public void testTryGetLockSuccessfully() throws LockServerException {
        String sessionID = _lockManager.tryGetLock("TEST_TRY_GET_LOCK_SUCCESSFULLY");
        Assert.assertNotNull(sessionID);
    }

    @Test (expected = LockServerException.class)
    public void testTryGetLockFailed() throws LockServerException {
        String lockName = "TEST_TRY_GET_LOCK_FAILED";
        _lockManager.getLock(lockName);
        _lockManager.tryGetLock(lockName);
    }

    @Test
    public void testUnlock() throws LockServerException {
        String notExistLockName = "NOT_EXIST_LOCK";
        _lockManager.unLock(notExistLockName);

        String testUnlockName = "TEST_UNLOCK_NAME";
        _lockManager.getLock(testUnlockName);
        _lockManager.unLock(testUnlockName);
        _lockManager.tryGetLock(testUnlockName);
    }

    @Test (expected = LockServerException.class)
    public void testHeartBeatFailWithNotExistLock() throws LockServerException {
        String notExistLockName = "NOT_EXIST_LOCK";
        String notExistSessionID = "NOT_EXIST_SESSION_ID";
        _lockManager.heartbeat(notExistLockName, notExistSessionID);
    }

    @Test (expected = LockServerException.class)
    public void testHeartBeatFailWithNotExistSessionID() throws LockServerException {
        String lockName = "HEARTBEAT_FAIL_WITH_INVALID_SESSION_ID";
        String notExistSessionID = "NOT_EXIST_SESSION_ID";
        _lockManager.getLock(lockName);
        _lockManager.heartbeat(lockName, notExistSessionID);
    }

    @Test
    public void testHeartBeatSuccessfully() throws LockServerException {
        String lockName = "HEARTBEAT_LOCK";
        String sessionID = _lockManager.getLock(lockName);
        _lockManager.heartbeat(lockName, sessionID);
    }

    @Test (expected = LockServerException.class)
    public void testHeartBeatFailWithTimeout() throws LockServerException {
        String lockName = "HEARTBEAT_LOCK_TIMEOUT";
        String sessionID = _lockManager.getLock(lockName);
        try {
            Thread.sleep((LockManager._LOCK_EXPIRATION_TIME_SECOND + 1) * 1000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        _lockManager.heartbeat(lockName, sessionID);
    }
}
