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
    public MutableLiveData<String> status = new MutableLiveData<>();

    public ReceiverThread(Socket socket, Context context) {
        this.context = context;
        this.socket = socket;
    }

    @Override
    public void run() {

        InputStream is;
        DataInputStream dis;
        FileOutputStream fos;

        String[] info;
        String location = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SendLite/";
        File root = new File(location);
        if (!root.exists()) {
            root.mkdirs();
        }
        System.out.println("TAAAG location:"+ location);
        int temp;
        long amountReceived, fileSize;
        byte[] buffer = new byte[1024 * 64];

        int i = 0;
        while (!exit) {
            i++;
            amountReceived = 0;
            System.out.println("TAAAG REc:"+ i);
            try {
                is = socket.getInputStream();
                dis = new DataInputStream(is);
                info = dis.readUTF().split(":");
                fos = new FileOutputStream(location + info[1]);
                fileSize = Long.parseLong(info[0]);

                status.postValue("Receiving:" + info[1]);
                bytesReceived.postValue(amountReceived);

                long s = System.currentTimeMillis();
                //Receiving Partial Data
                temp = is.read(buffer, 0, (int) (fileSize % buffer.length));
                fos.write(buffer, 0, temp);
                fileSize -= temp;
                amountReceived += temp;
                bytesReceived.postValue(amountReceived);

                //Receiving Remaining Data
                while (fileSize > 0) {
                    temp = is.read(buffer);
                    fos.write(buffer, 0, temp);
                    fileSize -= temp;
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
            }
        }
    }

    public void stopThread(){
        exit = true;
    }

}