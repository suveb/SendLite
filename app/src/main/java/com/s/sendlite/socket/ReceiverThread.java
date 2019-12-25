package com.s.sendlite.socket;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

public class ReceiverThread extends Thread {
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
        String location = Objects.requireNonNull(context.getExternalFilesDir("")).getAbsolutePath();
        int temp;
        long amountReceived, fileSize;
        byte[] buffer = new byte[1024 * 64];

        int i = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
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
}