/*
 *@Type SocketClientUsage.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 14:07
 * @version
 */
package example;

import client.Client;
import client.CmdClient;
import client.SocketClient;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.util.Scanner;


public class SocketClientUsage {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        Client client = new SocketClient(host, port);
        CmdClient cmdClient=new CmdClient(client);
        cmdClient.cot(args);

//        client.set("zsy12","for test");
//        client.get("zsy12");
//        client.rm("zsy12");
//        client.get("zsy12");
//        client.set("zsy14","for");
//        client.get("zsy14");
//        client.rm("zsy14");
//        client.get("zsy14");
    }
}