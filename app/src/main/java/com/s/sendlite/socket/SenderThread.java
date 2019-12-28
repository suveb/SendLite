package com.s.sendlite.socket;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.lifecycle.MutableLiveData;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SenderThread extends Thread {
    private Context context;
    private Socket socket;
    private Uri uri;

    public MutableLiveData<Long> byteSent = new MutableLiveData<>();
    public long fileSize = 0L;
    public MutableLiveData<String> status = new MutableLiveData<>();

    public SenderThread(Context context, Socket socket, Uri uri) {
        this.context = context;
        this.socket = socket;
        this.uri = uri;
    }

    @Override
    public void run() {
        InputStream is;
        OutputStream os;
        DataOutputStream dos;
        Cursor cursor;
        byte[] buffer = new byte[1024 * 64];

        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            is = context.getContentResolver().openInputStream(uri);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            int temp;
            Long amountSent = 0L;

            String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            String size = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
            fileSize = Long.parseLong(size);
            dos.writeUTF(size + ":" + name);

            status.postValue("Sending:" + name + ":" + size);
            byteSent.postValue(amountSent);

            //Sending Partial Data
            assert is != null;
            temp = is.read(buffer,0,Integer.parseInt(size)%buffer.length);
            os.write(buffer,0,temp);
            amountSent += temp;
            byteSent.postValue(amountSent);

            //Sending Remaining Data
            while ((temp = is.read(buffer)) != -1) {
                os.write(buffer);
                amountSent += temp;
                byteSent.postValue(amountSent);
            }
            status.postValue("Send Complete:" + name + ":" + size);

            os.flush();
            dos.flush();
            is.close();
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}