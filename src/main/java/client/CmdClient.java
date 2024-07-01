/*
 *@Type CmdClient.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 13:58
 * @version
 */
package client;

import org.apache.commons.cli.*;

public class CmdClient{
    public Client client;
    public CmdClient(Client client){
        this.client=client;
    }
    public void cot(String[]input){
        Options options = new Options();
        options.addOption("rm", true, "输入删除的键值");
        options.addOption("set", true, "输入查询");
        options.addOption("get", true, "输入添加");
        options.addOption("no",false,"退出");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, input);

            if (cmd.hasOption("rm")) {

                String keyToDelete = cmd.getOptionValue("rm");
                System.out.println(keyToDelete);
                client.rm(keyToDelete);
            }

            if (cmd.hasOption("set")) {
                String keyToDelete = cmd.getOptionValue("set");
                String[] add =keyToDelete.split("\\|");
                System.out.println(keyToDelete);
                client.set(add[0],add[1]);

            }

            if (cmd.hasOption("get")) {
                String keyToDelete = cmd.getOptionValue("get");
                String[] add =keyToDelete.split("\\|");
                System.out.println(keyToDelete);
                client.set(add[0],add[1]);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments. Please check the input.");
            System.out.println("NO");
        }
    }
}
