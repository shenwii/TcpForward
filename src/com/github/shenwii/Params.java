package com.github.shenwii;

/**
 * 入参类
 * @author shenwii
 */
public class Params {
    /**
     * 监听的地址
     */
    private String bindAddress = "0.0.0.0";
    /**
     * 监听的端口
     */
    private int bindPort = -1;
    /**
     * 转发的地址
     */
    private String forwardAddress = "127.0.0.1";
    /**
     * 转发的地址
     */
    private int forwardPort = -1;

    /**
     * 私有构造化
     */
    private Params() {

    }

    /**
     * 解析入参
     * @param argv 入参
     * @return 入参类
     * @throws IllegalArgumentException 参数异常
     */
    public static Params parse(String[] argv) throws IllegalArgumentException {
        Params params = new Params();
        for(int i = 0; i < argv.length; i += 2) {
            String p = argv[i];
            if("-b".equals(p) || "--bind".equals(p)) {
                if(i + 1 >= argv.length)
                    throw new IllegalArgumentException();
                params.setBindAddress(argv[i + 1]);
                continue;
            }
            if("-p".equals(p) || "--port".equals(p)) {
                if(i + 1 >= argv.length)
                    throw new IllegalArgumentException();
                try {
                    int port = Integer.parseInt(argv[i + 1]);
                    if(port <= 0 || port > 65535)
                        throw new IllegalArgumentException("port range must be (0, 65535].");
                    params.setBindPort(port);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("bind port number format error.");
                }
                continue;
            }
            if("-fa".equals(p) || "--forward-address".equals(p)) {
                if(i + 1 >= argv.length)
                    throw new IllegalArgumentException();
                params.setForwardAddress(argv[i + 1]);
                continue;
            }
            if("-fp".equals(p) || "--forward-port".equals(p)) {
                if(i + 1 >= argv.length)
                    throw new IllegalArgumentException();
                try {
                    int port = Integer.parseInt(argv[i + 1]);
                    if(port <= 0 || port > 65535)
                        throw new IllegalArgumentException("port range must be (0, 65535].");
                    params.setForwardPort(port);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("bind port number format error.");
                }
                continue;
            }
            throw new IllegalArgumentException();
        }
        if(params.getBindPort() == -1)
            throw new IllegalArgumentException("bind port can't be empty.");
        if(params.getForwardPort() == -1)
            throw new IllegalArgumentException("forward port can't be empty.");
        return params;
    }

    /**
     * 打印使用方法
     */
    public static void showUsage() {
        System.err.print("Usage: TcpForward [OPTION]\n" +
                "  -b, --bind                bind address, default: 0.0.0.0\n" +
                "  -p, --port                bind port\n" +
                "  -fa, --forward-address    tcp forward address, default: 127.0.0.1\n" +
                "  -fp, --forward-port       tcp forward port\n");
    }

    public String getBindAddress() {
        return bindAddress;
    }

    private void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getBindPort() {
        return bindPort;
    }

    private void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public String getForwardAddress() {
        return forwardAddress;
    }

    private void setForwardAddress(String forwardAddress) {
        this.forwardAddress = forwardAddress;
    }

    public int getForwardPort() {
        return forwardPort;
    }

    private void setForwardPort(int forwardPort) {
        this.forwardPort = forwardPort;
    }
}
