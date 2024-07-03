/*
 *@Type NormalStore.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:07
 * @version
 */
package service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.command.Command;
import model.command.CommandPos;
import model.command.RmCommand;
import model.command.SetCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.CommandUtil;
import utils.LoggerUtil;
import utils.RandomAccessFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LSMStore implements Store {

    public static final String TABLE = ".table";
    public static final String RW_MODE = "rw";
    public static final String NAME = "data";
    private final Logger LOGGER = LoggerFactory.getLogger(LSMStore.class);
    private final String logFormat = "[NormalStore][{}]: {}";


    /**
     * 内存表，类似缓存
     */
    private TreeMap<String, Command> memTable;
    /**
     * 备用内存表
     */
    private TreeMap<String,Command> oldmemTable;
    /**
     * hash索引，存的是数据长度和偏移量
     * */
    private HashMap<String, CommandPos> index;

    /**
     * 数据目录
     */
    private final String dataDir;

    /**
     * 读写锁，支持多线程，并发安全写入
     */
    private final ReentrantReadWriteLock indexLock;

    /**
     * 暂存数据的日志句柄
     */
    private RandomAccessFile writerReader;
    /**
     * 记录table文件的数量
     */
    private int tableNumber = 0;
    /**
     * table文件的容量
     */
    private static final long MAX_SIZE = 2*1024;
    /**
     * 持久化阈值
     */
    private final int storeThreshold = 2;
    private Rotate rotate;

    public LSMStore(String dataDir) {
        this.dataDir = dataDir;
        this.indexLock = new ReentrantReadWriteLock();
        this.memTable = new TreeMap<String, Command>();
        this.index = new HashMap<>();
        this.rotate = new Rotate();

        File file_s = new File(dataDir);
        if (!file_s.exists()) {
            LoggerUtil.info(LOGGER,logFormat, "NormalStore","dataDir isn't exist,creating...");
            file_s.mkdirs();
        }
        File dir = new File(this.dataDir);
        File[] files = dir.listFiles();
        if(files == null || files.length==0)
        {
            return;
        }
        else{
            for(File file:files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.equals("WAL.txt") && file.length() != 0) {
                    RandomAccessFileUtil.RedoWal(this.genFilePath(), this.dataDir + fileName);
                    this.reloadIndex(this.genFilePath());
                    RandomAccessFileUtil.cleanWal(this.dataDir + File.separator +"WAL.txt");
                } else if (file.isFile() && this.notDataTable(fileName)==true) {
                    tableNumber++;
                    rotate.tableIntoIndex(this.dataDir+fileName,tableNumber);
                }
                else{
                    this.reloadIndex(this.genFilePath());
                }
            }
        }
//        ArrayList<String> keys = new ArrayList<>(rotate.tableIndex.get(1).keySet());
//        Collections.reverse(keys);
//
//        // 反向遍历
//        for (String key : keys) {
//            System.out.println("Key: " + key + ", Value: " + rotate.tableIndex.get(1).get(key));
//        }
    }

    public String genFilePath() {

        return this.dataDir + File.separator + NAME + TABLE;

    }


    public void reloadIndex(String Path) {
        try {
            RandomAccessFile file = new RandomAccessFile(Path, RW_MODE);
            long len = file.length();
            long start = 0;
            file.seek(start);
            while (start < len) {
                int cmdLen = file.readInt();
                byte[] bytes = new byte[cmdLen];
                file.read(bytes);
                JSONObject value = JSON.parseObject(new String(bytes, StandardCharsets.UTF_8));
                Command command = CommandUtil.jsonToCommand(value);
                start += 4;
                if (command != null) {
                    CommandPos cmdPos = new CommandPos((int) start, cmdLen);
                    index.put(command.getKey(), cmdPos);
                }
                start += cmdLen;
            }
            file.seek(file.length());
            ArrayList<String> keys = new ArrayList<>(index.keySet());
            Collections.reverse(keys);

            // 反向遍历
            for (String key : keys) {
                System.out.println("Key: " + key + ", Value: " + index.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggerUtil.debug(LOGGER, logFormat, "reload index: "+index.toString());
    }

    @Override
    public void set(String key, String value) {
//        try {
//            SetCommand command = new SetCommand(key, value);
//            byte[] commandBytes = JSONObject.toJSONBytes(command);
//            // 加锁
//            indexLock.writeLock().lock();
//            // TODO://先写内存表，内存表达到一定阀值再写进磁盘
//            // 写table（wal）文件
//            RandomAccessFileUtil.writeInt(this.dataDir + File.separator +"WAL.txt" , commandBytes.length);
//            RandomAccessFileUtil.write(this.dataDir + File.separator +"WAL.txt" , commandBytes);
//            // 保存到memTable
//            memTable.put(key,command);
//            // TODO://判断是否需要将内存表中的值写回table
//            if(memTable.size()>=storeThreshold){
//                    new Thread(()->{
//                        oldmemTable = memTable;
//                        synchronized (memTable){
//                            memTable = new TreeMap<String, Command>();
//                        }
//                        indexLock.readLock().lock();
//                        for (Map.Entry<String, Command> entry : oldmemTable.entrySet()) {
//                            String onekey = entry.getKey();
//                            byte[] onecommandbytes = JSONObject.toJSONBytes(entry.getValue());
//                            RandomAccessFileUtil.writeInt(this.genFilePath(), onecommandbytes.length);
//                            int pos = RandomAccessFileUtil.write(this.genFilePath(), onecommandbytes);
//                            // 添加索引
//                            CommandPos cmdPos = new CommandPos(pos, onecommandbytes.length);
//                            index.put(onekey, cmdPos);
//                        }
//                        indexLock.readLock().unlock();
//                        oldmemTable.clear();
//                        RandomAccessFileUtil.cleanWal(this.dataDir + File.separator +"WAL.txt");
//                    }).start();
//                }
//            judgeFileLoad(this.genFilePath());
//            rotate.fileZip(tableNumber,dataDir);
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        } finally {
//            indexLock.writeLock().unlock();
//        }
    }

    @Override
    public String get(String key) {
        try {
            if(memTable!=null && memTable.get(key)!=null){
                if (memTable.get(key) instanceof SetCommand) {
                    return ((SetCommand) memTable.get(key)).getValue();
                }
                if (memTable.get(key) instanceof RmCommand) {
                    return null;
                }
            } else if (oldmemTable != null && oldmemTable.get(key)!=null) {
                if (oldmemTable.get(key) instanceof SetCommand) {
                    return ((SetCommand) oldmemTable.get(key)).getValue();
                }
                if (oldmemTable.get(key) instanceof RmCommand) {
                    return null;
                }
            }
            else{
                indexLock.readLock().lock();
                // 从索引中获取信息
                CommandPos cmdPos = index.get(key);
                if (cmdPos == null) {
                    indexLock.readLock().unlock();
                    return rotate.findUnchangeTable(key,this.dataDir);
                }
                byte[] commandBytes = RandomAccessFileUtil.readByIndex(this.genFilePath(), cmdPos.getPos(), cmdPos.getLen());

                JSONObject value = JSONObject.parseObject(new String(commandBytes));
                Command cmd = CommandUtil.jsonToCommand(value);
                if (cmd instanceof SetCommand) {
                    indexLock.readLock().unlock();
                    return ((SetCommand) cmd).getValue();
                }
                if (cmd instanceof RmCommand) {
                    indexLock.readLock().unlock();
                    return null;
                }
            }
            judgeFileLoad(this.genFilePath());
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        return null;
    }

    @Override
    public void rm(String key) {
//        try {
//            RmCommand command = new RmCommand(key);
//            byte[] commandBytes = JSONObject.toJSONBytes(command);
//            // 加锁
//            indexLock.writeLock().lock();
//            // TODO://先写内存表，内存表达到一定阀值再写进磁盘
//            // 写table（wal）文件
//            RandomAccessFileUtil.writeInt(this.dataDir + File.separator +"WAL.txt", commandBytes.length);
//            RandomAccessFileUtil.write(this.dataDir + File.separator +"WAL.txt", commandBytes);
//            // 保存到memTable
//            memTable.put(key,command);
//            // TODO://判断是否需要将内存表中的值写回table
//            if(memTable.size()>=storeThreshold){
//                synchronized (memTable)
//                {
//                    oldmemTable = memTable;
//                    memTable = new TreeMap<String, Command>();
//                    new Thread(()->{
//                        indexLock.readLock().lock();
//                        for (Map.Entry<String, Command> entry : oldmemTable.entrySet()) {
//                            String onekey = entry.getKey();
//                            byte[] onecommandbytes = JSONObject.toJSONBytes(entry.getValue());
//                            RandomAccessFileUtil.writeInt(this.genFilePath(), onecommandbytes.length);
//                            int pos = RandomAccessFileUtil.write(this.genFilePath(), onecommandbytes);
//                            // 添加索引
//                            CommandPos cmdPos = new CommandPos(pos, onecommandbytes.length);
//                            index.put(onekey, cmdPos);
//                        }
//                        indexLock.readLock().unlock();
//                        oldmemTable.clear();
//                        RandomAccessFileUtil.cleanWal(this.dataDir + File.separator +"WAL.txt");
//                    }).start();
//                }
//            }
//            judgeFileLoad(this.genFilePath());
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        } finally {
//            indexLock.writeLock().unlock();
//        }
    }

    @Override
    public void close() throws IOException {
        try {
        oldmemTable = memTable;
        memTable = new TreeMap<>();
        for (Map.Entry<String, Command> entry : oldmemTable.entrySet()) {
            String onekey = entry.getKey();
            byte[] onecommandbytes = JSONObject.toJSONBytes(entry.getValue());
            RandomAccessFileUtil.writeInt(this.genFilePath(), onecommandbytes.length);
            int pos = RandomAccessFileUtil.write(this.genFilePath(), onecommandbytes);
            // 添加索引
            CommandPos cmdPos = new CommandPos(pos, onecommandbytes.length);
            index.put(onekey, cmdPos);
        }
            judgeFileLoad(this.genFilePath());
            RandomAccessFileUtil.cleanWal(this.dataDir + File.separator +"WAL.txt");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean notDataTable(String fileName){
        Pattern pattern = Pattern.compile("^data\\d+\\.table$");
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }
    public void judgeFileLoad(String filePath) throws Exception {
        try{
            RandomAccessFile file = new RandomAccessFile(filePath,RW_MODE);
            if(file.length()>MAX_SIZE){
                file.close();
                indexLock.writeLock().lock();
                tableNumber++;
                rotate.rotateTable(this.dataDir,filePath,index,tableNumber);
                RandomAccessFile newfile = new RandomAccessFile(filePath,RW_MODE);
                newfile.seek(0);
                newfile.setLength(0);
                index.clear();
                indexLock.writeLock().unlock();
                newfile.close();
            }
            else{
                return;
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
