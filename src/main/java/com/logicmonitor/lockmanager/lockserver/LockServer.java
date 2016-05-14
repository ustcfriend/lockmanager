package com.logicmonitor.lockmanager.lockserver;

import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;

import java.io.IOException;

/**
 * Created by kai on 16/5/14.
 */
public class LockServer {

    private static void _usage() {
        System.out.println("Usage:   java com.logicmonitor.lockmanager.lockserver.LockServer port\n" +
                                "Example: java com.logicmonitor.lockmanager.lockserver.LockServer 9090");
    }




    public static void main(String args[]) {
        //Because this is a toy project, we just start server with port number
        if (args.length != 1) {
            _usage();
            System.exit(1);
        }
        int port = Integer.valueOf(args[0]);

        try {
            LockServerHandler handler = new LockServerHandler();
            LockServerApplication.Processor processor = new LockServerApplication.Processor<LockServerHandler>(handler);
            //TServerTransport serverTransport = new TServerSocket(port);
            //TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(port);
            TThreadedSelectorServer.Args options =
                    new TThreadedSelectorServer.Args(serverSocket)
                            .processor(processor)
                            .workerThreads(10)
                            .selectorThreads(4);
            TServer server = new TThreadedSelectorServer(options);


            //TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
            //TServer server = new TNonblockingServer(new TNonblockingServer.Args(serverTransport).processor(processor));
            server.serve();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }
}
