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

public class SocketClientUsage {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;
        Client client = new SocketClient(host, port);
//        CmdClient cmd = new CmdClient(client);
//        cmd.cot();
//        for(int i = 100; i <=120;i++)
//        {
//            client.set("zsy"+i,"let"+i+"go to see the world!!");
////            client.rm("zsy"+i);
//        }
        client.set("zsy99","let 99 go to see the world!!");
        client.set("zsy106","let 106 go to see the world");
//        client.rm("zsy17");
    }
}