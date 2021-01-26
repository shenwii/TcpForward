# TcpForward

## 介绍

用NIO实现的TCP转发服务器

## 功能

    TCP流量转发

## 需求
* JDK1.8+

## 使用方法

    java -jar TcpForward.jar [OPTION]
        -b, --bind                bind address, default: 0.0.0.0
        -p, --port                bind port
        -fa, --forward-address    tcp forward address, default: 127.0.0.1
        -fp, --forward-port       tcp forward port
