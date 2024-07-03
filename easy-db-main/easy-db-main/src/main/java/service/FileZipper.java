package service;

import com.alibaba.fastjson.JSONObject;
import model.command.Command;
import model.command.CommandPos;
import model.command.RmCommand;
import model.command.SetCommand;
import utils.CommandUtil;
import utils.RandomAccessFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class FileZipper {
    private RandomAccessFile file1;
    private RandomAccessFile file2;
    private final long MAX_SIZE = 2*1024;
    private  final String RW_MODE = "rw";
    private  final String TABLE = ".table";
    public FileZipper(RandomAccessFile file1,RandomAccessFile file2){
        this.file1 = file1;
        this.file2 = file2;
    }
    public void ZipFiles(String dataDir, TreeMap<Integer,HashMap<String,CommandPos>> tableIndex,int i,int newNumber){
        try{
            File deleteFile1 = new File(dataDir+"data"+i+TABLE);
            File deleteFile2 = new File(dataDir+"data"+(i+1)+TABLE);
                HashMap<String, CommandPos> index1 = tableIndex.get(i);
                HashMap<String,CommandPos> index2 = tableIndex.get(i+1);
                //获取新表中的键
            findIndex2_key(index2,index1);
            System.out.println(file1.length());
                ArrayList<String>index1_keys = new ArrayList<>(index1.keySet());
                for(String key:index1_keys){
                    System.out.println(key+"and"+index1.get(key));
                }
                int start = 4;
                RandomAccessFile file = new RandomAccessFile(dataDir+"newData"+newNumber+TABLE,RW_MODE);
                for(String key:index1_keys){
                    int comPosLength = index1.get(key).getPos();
                    //判断数据是在新表还是旧表中（长度超过就是新表）
                    if(comPosLength>=(int)file1.length()){
                        index1.get(key).setPos(comPosLength-(int)file1.length());
                        start = writeNewDataTable(file,index1,key,i,dataDir,start);
                    }
                    else{
                        CommandPos cmdPos = index1.get(key);
                        System.out.println(cmdPos.getPos()+"and"+cmdPos.getLen());
                        byte[] bytes = RandomAccessFileUtil.readByIndex(dataDir+"data"+i+TABLE,cmdPos.getPos(),cmdPos.getLen());
                        file.writeInt(cmdPos.getLen());
                        file.write(bytes);
                        index1.get(key).setPos(start);
                        start += (4 + cmdPos.getLen());
                    }
                }
                file1.close();
                file2.close();
                file.close();
                System.out.println(index1);
                tableIndex.put(newNumber,index1);
                boolean file1True =  deleteFile1.delete();
                boolean file2True = deleteFile2.delete();
                System.out.println(file1True);
                System.out.println(file2True);
                File oldfile = new File(dataDir+"newData"+newNumber+TABLE);
                File newfile = new File(dataDir+"data"+newNumber+TABLE);
                oldfile.renameTo(newfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void findIndex2_key(HashMap<String, CommandPos> index2,HashMap<String, CommandPos> index1){
        try{
            ArrayList<String> index2_keys = new ArrayList<>(index2.keySet());
            for(String key:index2_keys){
                System.out.println(index2.get(key));
                int oldPos = index2.get(key).getPos();
                index2.get(key).setPos(oldPos+(int)file1.length());
                index1.put(key,index2.get(key));
            }
            }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public int writeNewDataTable(RandomAccessFile file,HashMap<String, CommandPos> index1,String key,int i,String dataDir,int start){
        try{
            CommandPos cmdPos = index1.get(key);
            System.out.println(cmdPos.getPos()+"and"+cmdPos.getLen());
            byte[] bytes = RandomAccessFileUtil.readByIndex(dataDir+"data"+(i+1)+TABLE,cmdPos.getPos(),cmdPos.getLen());
            JSONObject value = JSONObject.parseObject(new String(bytes));
            Command cmd = CommandUtil.jsonToCommand(value);
            if(cmd instanceof SetCommand){
                file.writeInt(cmdPos.getLen());
                file.write(bytes);
                index1.get(key).setPos(start);
                start += (4 + cmdPos.getLen());
            }
            if(cmd instanceof RmCommand){
                index1.remove(key);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return start;
    }
}
