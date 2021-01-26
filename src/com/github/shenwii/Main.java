package com.github.shenwii;

import java.io.IOException;

/**
 * Main
 * @author shenwii
 */
public class Main {

    /**
     * 程序入口
     * @param argv 入参
     * @throws IOException IO异常
     */
    public static void main(String[] argv) throws IOException {
        Params params;
        try {
            params = Params.parse(argv);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if(msg != null && !"".equals(msg))
                System.err.println(msg);
            Params.showUsage();
            return;
        }
        TcpForwardServer tcpForwardServer = new TcpForwardServer(params.getBindAddress(), params.getBindPort(), params.getForwardAddress(), params.getForwardPort());
        tcpForwardServer.startForever();
    }
}
