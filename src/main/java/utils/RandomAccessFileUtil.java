/*
 *@Type RandomAccessFileUtil.java
 * @Desc
 * @Author urmsone urmsone@163.com
 * @date 2024/6/13 02:58
 * @version
 */
package utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RandomAccessFileUtil {

    private static final String RW_MODE = "rw";
    private static final ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();

    public static int write(String filePath, byte[] value) {
        RandomAccessFile file = null;
        long len = -1L;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            len = file.length();
            file.seek(len);
            file.write(value);
            if(file.length()>=5000){

            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (int)len;
    }
    public static void writeInt(String filePath, int value) {
        RandomAccessFile file = null;
        long len = -1L;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            len = file.length();
            file.seek(len);
            file.writeInt(value);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readByIndex(String filePath, int index, int len) {
        RandomAccessFile file = null;
        byte[] res = new byte[len];
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            file.seek((long)index);
            file.read(res, 0, len);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static void cleanWal(String filePath) {
        RandomAccessFile file = null;
        try {
            indexLock.writeLock().lock();
            file = new RandomAccessFile(filePath, RW_MODE);
            file.seek(0);
            file.setLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            indexLock.writeLock().unlock();
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void RedoWal(String filePath,String WalPath) {
        RandomAccessFile file = null;
        RandomAccessFile Wal = null;
        long len = -1L;
        try {
            file = new RandomAccessFile(filePath, RW_MODE);
            len = file.length();
            file.seek(len);
            Wal = new RandomAccessFile(WalPath,RW_MODE);
            byte[] bytes = new byte[2048];
            int length = 0;
            while((length = Wal.read(bytes))!=-1){
                file.write(bytes,0,length);
            }
            Wal.seek(0);
            Wal.setLength(0);
            Wal.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
