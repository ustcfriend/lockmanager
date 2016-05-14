package com.logicmonitor.lockmanager.lockserver;


/**
 * Created by kai on 16/5/14.
 */
public class LockServerHandler implements LockServerApplication.Iface {


    @Override
    public String lock(String lockName) {
        System.out.println(String.format("lock start:%s", lockName));
        String sessionID = LockManager.getInstance().getLock(lockName);
        System.out.println(String.format("lock end:%s", lockName));
        return sessionID;
    }

    @Override
    public String tryLock(String lockName) throws LockServerException {
        System.out.println(String.format("tryLock start:%s", lockName));
        try {
            String sessionID = LockManager.getInstance().tryGetLock(lockName);
            return sessionID;
        }
        finally {
            System.out.println(String.format("tryLock end:%s", lockName));
        }
    }

    @Override
    public void unLock(String lockName) throws LockServerException {
        System.out.println(String.format("unLock start:%s", lockName));
        try {
            LockManager.getInstance().unLock(lockName);
        }
        finally {
            System.out.println(String.format("unLock end:%s", lockName));
        }
    }

    @Override
    public void heartBeat(String lockName, String sessionID) throws LockServerException {
        System.out.println(String.format("heartBeat start, lockName=%s, sessionID=%s", lockName, sessionID));
        try {
            LockManager.getInstance().heartbeat(lockName, sessionID);
        }
        finally {
            System.out.println(String.format("heartBeat end, lockName=%s, sessionID=%s", lockName, sessionID));
        }
    }

    @Override
    public String getAllLock() {
        return LockManager.getInstance().getAllLock();
    }

}
