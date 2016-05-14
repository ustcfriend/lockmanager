package com.logicmonitor.lockmanager.lockserver;

import com.sun.tools.corba.se.idl.InvalidArgument;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.*;

/**
 * Created by kai on 16/5/14.
 */
public class LockClient {

    private static final String _OPERATION_LOCK     = "lock";
    private static final String _OPERATION_TRY_LOCK = "tryLock";
    private static final String _OPERATION_UNLOCK   = "unLock";
    private static final String _OPERATION_HEARTBEAT= "heartBeat";
    private static final String _OPERATION_GET_ALL_LOCK = "getAllLock";

    private static final int _FIXED_ARGS_NUMBER = 3;
    private static final int _HEARTBEAT_ARGS_NUMBER = 2;
    private static final int _GET_ALL_LOCK_ARGS_NUMBER = 0;

    private static final int _ARG_IP_INDEX = 0;
    private static final int _ARG_PORT_INDEX = 1;
    private static final int _ARG_OPERATION_INDEX = 2;
    private static final int _ARG_LOCKNAME_INDEX = 3;
    private static final int _ARG_LOCKSESSION_ID_INDEX = 4;

    private static TTransport _tranport;
    private static TProtocol _protocol;
    private static LockServerApplication.Client _client;

    private static void _usage() {
        System.out.println("Usage:   java com.logicmonitor.lockmanager.lockserver.LockClient ip port operation [lockname] [sessionid]\n" +
                           "Example: java com.logicmonitor.lockmanager.lockserver.LockClient 127.0.0.1 9090 lock kai\n" +
                           "Example: java com.logicmonitor.lockmanager.lockserver.LockClient 127.0.0.1 9090 lock kai 12345678");
    }

    private static void _init(String ip, int port) throws Exception {
        _tranport = new TFramedTransport(new TSocket(ip, port));
        _tranport.open();
        _protocol = new TBinaryProtocol(_tranport);
        _client = new LockServerApplication.Client(_protocol);
    }

    private static void _checkArgs(String operation, String[] args) throws Exception {
        if (args.length == _FIXED_ARGS_NUMBER && !operation.equalsIgnoreCase(_OPERATION_GET_ALL_LOCK)) {
            throw new InvalidArgument(String.format("Invalid args=%s, operation=%s", args.toString(), operation));
        }
        if (args.length < (_FIXED_ARGS_NUMBER + _HEARTBEAT_ARGS_NUMBER) && operation.equalsIgnoreCase(_OPERATION_HEARTBEAT)) {
            throw new InvalidArgument(String.format("Invalid args=%s, operation=%s", args.toString(), operation));
        }
    }

    private static void _process(String operation, String[] args) throws Exception {
        _checkArgs(operation, args);
        if (operation.equalsIgnoreCase(_OPERATION_LOCK)) {
            String sessionID = _client.lock(args[_ARG_LOCKNAME_INDEX]);
            System.out.println(String.format("sessionID=%s", sessionID));
            return;
        }
        if (operation.equalsIgnoreCase(_OPERATION_TRY_LOCK)) {
            try {
                String sessionID = _client.tryLock(args[_ARG_LOCKNAME_INDEX]);
                System.out.println(String.format("sessionID=%s", sessionID));
            }
            catch (LockServerException e) {
                System.out.println(String.format("fail to try lock:%s", e.getMessage()));
            }
            return;
        }
        if (operation.equalsIgnoreCase(_OPERATION_UNLOCK)) {
            try {
                _client.unLock(args[_ARG_LOCKNAME_INDEX]);
            }
            catch (LockServerException e) {
                System.out.println(String.format("fail to unlock:%s", e.getMessage()));
            }
            return;
        }
        if (operation.equalsIgnoreCase(_OPERATION_HEARTBEAT)) {
            try {
                _client.heartBeat(args[_ARG_LOCKNAME_INDEX], args[_ARG_LOCKSESSION_ID_INDEX]);
            }
            catch (LockServerException e) {
                System.out.println(String.format("fail to heartbeat:%s", e.getMessage()));
            }
            return;
        }
        if (operation.equalsIgnoreCase(_OPERATION_GET_ALL_LOCK)) {
            String allLock = _client.getAllLock();
            System.out.println(String.format("alllock=%s", allLock));
        }
    }

    public static void main(String args[]) throws Exception {
        if (args.length < _FIXED_ARGS_NUMBER) {
            _usage();
            System.exit(1);
        }

        String ip = args[_ARG_IP_INDEX];
        int port = Integer.valueOf(args[_ARG_PORT_INDEX]);
        System.out.println(String.format("serverip=%s, port=%d", ip, port));
        _init(ip, port);

        _process(args[_ARG_OPERATION_INDEX], args);
    }

}
