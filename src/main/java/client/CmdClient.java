/*
 *@Type CmdClient.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 13:58
 * @version
 */
package client;



import java.util.Scanner;

public class CmdClient{
    public Client client;
    public CmdClient(Client client){
        this.client=client;
    }
    public void cot() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("输入指令：(set <key> <value>/get <key>/rm <key>/close(结束))(空格隔开)");
            String is = scanner.nextLine();
            String[] add = is.split("\\s+");
            switch (add[0]) {
                case "set":
                    if (add.length == 3) {
                        client.set(add[1], add[2]);
                    } else {
                        System.err.println("使用方法: set <key> <value>");
                    }
                    break;
                case "get":
                    if (add.length == 2) {
                        client.get(add[1]);
                    } else {
                        System.err.println("使用方法: get <key>");
                    }
                    break;
                case "rm":
                    if (add.length == 2) {
                        client.rm(add[1]);
                        System.out.println("键 \"" + add[1] + "\" 已被移除。");
                    } else {
                        System.err.println("使用方法: rm <key>");
                    }
                    break;
                case "close":
                    System.out.println("退出程序...");
                    scanner.close();
                    return;
                default:
                    System.err.println("命令错误: " + add[0] + "请重新输入");
                    break;
            }

        }

    }
}
