namespace java com.logicmonitor.lockmanager.lockserver

exception LockServerException {
    1: string message
}

service LockServerApplication {

    string lock(1:string lockName),

    string tryLock(1:string lockName) throws (1:LockServerException ex),

    void unLock(1:string lockName) throws (1:LockServerException ex),

    void heartBeat(1:string lockName, 2:string sessionID) throws (1:LockServerException ex),

    string getAllLock()
}