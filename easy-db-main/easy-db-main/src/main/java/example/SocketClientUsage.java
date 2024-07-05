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
        for(int i = 1; i <=29;i++)
        {
            client.set("whh"+i,"let"+i+"go to see the world!!");
        }
        client.set("zsy900","let me go to see the world!!");
        client.set("zsy901","let 106 go to see the world");
        client.set("zsy902","let 7867 go to see the world");
//        client.set("zsy905","let 999 go to see the world");
//        client.set("zsy906","let 109 go to see the world");
//        client.rm("zsy17");
    }
}