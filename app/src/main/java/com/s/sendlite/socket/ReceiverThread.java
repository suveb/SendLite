package com.s.sendlite.socket;

import android.content.Context;
import android.os.Environment;

import androidx.lifecycle.MutableLiveData;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

public class ReceiverThread extends Thread {
    private volatile boolean exit = false;
    private Context context;
    private Socket socket;

    public MutableLiveData<Long> bytesReceived = new MutableLiveData<>();
    public long fileSize = 0L;
    public MutableLiveData<String> status = new MutableLiveData<>();

    public ReceiverThread(Socket socket, Context context) {
        this.context = context;
        this.socket = socket;
    }

    private static boolean isSpaceAvailable(long fileSize) {
        long freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
        System.out.println("TAAAG FreeSpace:" + freeSpace / 1024 / 1024);
        return (freeSpace - fileSize) > 50 * 1024 * 1024;
    }

    public void stopThread() {
        exit = true;
    }

    @Override
    public void run() {
        InputStream is;
        DataInputStream dis;
        FileOutputStream fos;
        String location;
        String[] info;
        int temp;
        long amountReceived, size;
        byte[] buffer = new byte[1024 * 64];

        int i = 0;
        while (!exit) {
            i++;
            fileSize = 0L;
            amountReceived = 0;
            System.out.println("TAAAG ReceiverThreadNo:" + i);
            try {
                is = socket.getInputStream();
                dis = new DataInputStream(is);
                info = dis.readUTF().split(":");
                size = Long.parseLong(info[0]);
                fileSize = size;
                location = getFilePath(info[1], size);
                if (location.equals("false")) {
                    status.postValue("Storage Is Not Available");
                    continue;
                }
                System.out.println("TAAAG location:" + location);
                fos = new FileOutputStream(location);

                status.postValue("Receiving:" + info[1]);
                bytesReceived.postValue(amountReceived);

                long s = System.currentTimeMillis();
                //Receiving Partial Data
                temp = is.read(buffer, 0, (int) (size % buffer.length));
                fos.write(buffer, 0, temp);
                size -= temp;
                amountReceived += temp;
                bytesReceived.postValue(amountReceived);

                //Receiving Remaining Data
                while (size > 0) {
                    temp = is.read(buffer);
                    fos.write(buffer, 0, temp);
                    size -= temp;
                    amountReceived += temp;
                    bytesReceived.postValue(amountReceived);
                }
                System.out.println("TAAAG Time:" + (System.currentTimeMillis() - s));

//            while ((temp = is.read(buffer)) > 0) {
//                fos.write(buffer, 0, temp);
//                amountReceived += temp;
//                bytesReceived.postValue(amountReceived);
//                if (fileSize < amountReceived) break;
//            }

                status.postValue("Receive Complete:" + info[1]);

                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                stopThread();
            }
        }
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    private String getFilePath(String fileName, long fileSize) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly() || !isSpaceAvailable(fileSize)) {
            return "false";
        }
        String location = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SendLite/";
        File root = new File(location);
        if (!root.exists()) {
            root.mkdirs();
        }
        File rootFile = new File(location + fileName);
        int i = 0;
        while (rootFile.exists()) {
            i++;
            rootFile = new File(location + i + ")" + fileName);
        }
        return rootFile.getAbsolutePath();
    }
}