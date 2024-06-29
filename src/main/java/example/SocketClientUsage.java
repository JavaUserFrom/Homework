/*
 *@Type SocketClientUsage.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 14:07
 * @version
 */
package example;

import client.Client;
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
//        client.get("zsy1");
        Options options = new Options();
        options.addOption("rm", true, "输入删除的键值");
        options.addOption("set", true, "输入查询");
        options.addOption("get", true, "输入添加");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("rm")) {

                String keyToDelete = cmd.getOptionValue("rm");

                System.out.println(keyToDelete);
                client.rm(keyToDelete);
            }

            if (cmd.hasOption("set")) {
                String keyToDelete = cmd.getOptionValue("set");
                String add[]=keyToDelete.split("\\|");
                System.out.println(keyToDelete);
                client.set(add[0],add[1]);

            }

            if (cmd.hasOption("get")) {
                String keyToDelete = cmd.getOptionValue("get");
                String add[]=keyToDelete.split("\\|");
                System.out.println(keyToDelete);
                client.set(add[0],add[1]);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments. Please check the input.");
            System.out.println("NO");
        }
//        client.set("zsy12","for test");
//        client.get("zsy12");
//        client.rm("zsy12");
//        client.get("zsy12");
    }
}