package service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.command.Command;
import model.command.CommandPos;
import model.command.RmCommand;
import model.command.SetCommand;
import utils.CommandUtil;
import utils.RandomAccessFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Rotate {
    private static final String RW_MODE = "rw";
    private static final String TABLE = ".table";
    private final ReentrantReadWriteLock indexLock;

    /**
     * 存储内存中各个表的索引，方便后续查询
     */
    public TreeMap<Integer, HashMap<String, CommandPos>> tableIndex;
    private static final long MAX_SIZE = 2 * 1024;

    public Rotate() {
        this.tableIndex = new TreeMap<>();
        this.indexLock = new ReentrantReadWriteLock();
    }

    public void rotateTable(String dataDir, String filePath, HashMap<String, CommandPos> index, int length) throws Exception {
        try {
            if (length > 1) {
                RandomAccessFile file = new RandomAccessFile(dataDir + "data" + length + TABLE, RW_MODE);
                ArrayList<String> keys = new ArrayList<>(index.keySet());
                int start = 0;
                for (String key : keys) {
                    CommandPos cmdPos = index.get(key);
                    byte[] bytes = RandomAccessFileUtil.readByIndex(filePath, cmdPos.getPos(), cmdPos.getLen());
                    file.writeInt(cmdPos.getLen());
                    file.write(bytes);
                    index.get(key).setPos(start);
                    start += (4 + cmdPos.getLen());
            }
                tableIndex.put(length, index);
                this.fileZip(length, dataDir);
            } else {
                RandomAccessFile file = new RandomAccessFile(dataDir + "data" + length + TABLE, RW_MODE);
                ArrayList<String> keys = new ArrayList<>(index.keySet());
                int start = 0;
                for (String key : keys) {
                    CommandPos cmdPos = index.get(key);
                    byte[] bytes = RandomAccessFileUtil.readByIndex(filePath, cmdPos.getPos(), cmdPos.getLen());
                    file.writeInt(cmdPos.getLen());
                    file.write(bytes);
                    index.get(key).setPos(start);
                    start += (4 + cmdPos.getLen());

                }
                tableIndex.put(length, index);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void tableIntoIndex(String filePath, int length) {
        try {
            HashMap<String, CommandPos> index = new HashMap<>();
            RandomAccessFile file = new RandomAccessFile(filePath, RW_MODE);
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
            tableIndex.put(length, index);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String findUnchangeTable(String key, String fileDir) {
        String result = null;
        for (int i = tableIndex.size(); i > 0; i--) {
            HashMap<String, CommandPos> index = tableIndex.get(i);
            indexLock.readLock().lock();
            // 从索引中获取信息
            CommandPos cmdPos = index.get(key);
            if (cmdPos == null) {
                indexLock.readLock().unlock();
                continue;
            }
            byte[] commandBytes = RandomAccessFileUtil.readByIndex(fileDir + "data" + i + TABLE, cmdPos.getPos(), cmdPos.getLen());

            JSONObject value = JSONObject.parseObject(new String(commandBytes));
            Command cmd = CommandUtil.jsonToCommand(value);
            if (cmd instanceof SetCommand) {
                indexLock.readLock().unlock();
                result = ((SetCommand) cmd).getValue();
                break;
            }
            if (cmd instanceof RmCommand) {
                indexLock.readLock().unlock();
                break;
            }
        }
        return result;
    }

    public void fileZip(int length, String dataDir) throws Exception {
        new Thread(() -> {
            try {
                if (length >= 2) {
                    int newNumber = 1;
                    indexLock.writeLock().lock();
                    System.out.println("------------文件正在压缩中----------");
                    for (int i = 1; i <= tableIndex.size(); i += 2) {
                        File deleteFile1 = new File(dataDir + "data" + i + TABLE);
                        File deleteFile2 = new File(dataDir + "data" + (i + 1) + TABLE);
                        if (!deleteFile1.exists() || !deleteFile2.exists()) {
                            deleteFile1.renameTo(new File(dataDir + "data" + newNumber + TABLE));
                            break;
                        }
                        //旧表
                        RandomAccessFile file1 = new RandomAccessFile(dataDir + "data" + i + TABLE, RW_MODE);
                        //新表
                        RandomAccessFile file2 = new RandomAccessFile(dataDir + "data" + (i + 1) + TABLE, RW_MODE);
                        FileZipper fileZipper = new FileZipper(file1, file2);
                        fileZipper.ZipFiles(dataDir,tableIndex,i,newNumber);
                        newNumber++;
                    }
                    indexLock.writeLock().unlock();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();

    }
}

