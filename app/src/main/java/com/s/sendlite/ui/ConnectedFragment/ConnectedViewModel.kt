package com.s.sendlite.ui.ConnectedFragment

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s.sendlite.dataClass.History
import com.s.sendlite.dataClass.Repository
import kotlinx.coroutines.launch
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.URLConnection
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class ConnectedViewModel(private val repository: Repository) : ViewModel() {
    val peerName = MutableLiveData<String>()
    val status = MutableLiveData<String>()
    val bytes = MutableLiveData<Long>()
    val fileSize = MutableLiveData<Long>()
    val elapsedTime = MutableLiveData<Long>()
    val socketStatus = MutableLiveData<String>("connecting")
    val connectionStatus = MutableLiveData<Boolean>(false)

    private var socket = Socket()

    @Volatile
    private var exit = false

    fun isConnected() = if (!socket.isClosed && socket.isConnected) {
        socketStatus.postValue("still connected")
        connectionStatus.postValue(true)
        true
    } else {
        connectionStatus.postValue(false)
        socketStatus.postValue("disconnected")
        false
    }

    fun closeSocket(){
        if(!socket.isClosed)
            socket.close()
    }

    fun initialise(memberType: String, hostAddress: String, myName: String) = Thread {
        try {
            if (memberType == "Server") {
                initServer(myName)
            } else if (memberType == "Client") {
                initClient(hostAddress, myName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            connectionStatus.postValue(false)
            socketStatus.postValue("initialise failed") //TODO again establish connection
        }
    }.start()

    private fun initServer(myName: String) {
        val server = ServerSocket(12327)
        socket = server.accept()

        //Sending Server Name
        DataOutputStream(socket.getOutputStream()).writeUTF(myName)
        //Receiving Client Name
        peerName.postValue(DataInputStream(socket.getInputStream()).readUTF())
        connectionStatus.postValue(true)
        socketStatus.postValue("connected")
        receiveFile()
    }

    private fun initClient(hostAddress: String, myName: String) {
        socket = Socket(hostAddress, 12327)

        //Receiving Server Name
        peerName.postValue(DataInputStream(socket.getInputStream()).readUTF())
        //Sending Client Name
        DataOutputStream(socket.getOutputStream()).writeUTF(myName)

        connectionStatus.postValue(true)
        socketStatus.postValue("connected")
        receiveFile()
    }

    fun sendFile(context: Context, fileUri: Uri) = Thread {
        socketStatus.postValue("sending")
        val fileInfo = History()
        try {
            val buffer = ByteArray(1024 * 64)
            val cursor = context.contentResolver.query(fileUri, null, null, null, null)
            cursor?.moveToFirst()
            val name = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))!!
            fileSize.postValue(cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE)).toLong())

            val `is` = context.contentResolver.openInputStream(fileUri)!!
            val os = socket.getOutputStream()!!
            val dos = DataOutputStream(os)

            var temp: Int
            var amountSent = 0L

            dos.writeUTF("${fileSize.value}:$name")
            status.postValue(name)
            bytes.postValue(amountSent)

            fileInfo.apply {
                fileName = name
                fileSize = sizeInWord(this@ConnectedViewModel.fileSize.value!!)
                fileType = getFileType(name)
                fileLocation = fileUri.path!!
                senderName = "self"
                receiverName = peerName.value!!
            }
            val startTime = System.currentTimeMillis()

            //Sending Partial Data
            temp = `is`.read(buffer, 0, (fileSize.value!! % buffer.size).toInt())
            os.write(buffer, 0, temp)
            amountSent += temp.toLong()
            bytes.postValue(amountSent)

            //Sending Remaining Data
            while (`is`.read(buffer).also { temp = it } != -1) {
                os.write(buffer)
                amountSent += temp.toLong()
                bytes.postValue(amountSent)
                elapsedTime.postValue(System.currentTimeMillis() - startTime)
            }

            status.postValue("Send Complete:$name")
            fileInfo.status = "Success"

            socketStatus.postValue("free")

            os.flush()
            dos.flush()
            `is`.close()
            cursor.close()
        } catch (e: IOException) {
            fileSize.postValue(0)
            fileInfo.status = "Failure"
            socketStatus.postValue("SendThread Failed")//TODO handle it
            e.printStackTrace()
        } finally {
            isConnected()
            if (fileInfo.status != "")
                viewModelScope.launch {
                    fileInfo.dateReceived = Date(System.currentTimeMillis())
                    repository.insertFile(fileInfo)
                }
        }
    }.start()

    private fun receiveFile() {
        var `is`: InputStream
        var dis: DataInputStream
        var fos: FileOutputStream
        var location: String
        var info: Array<String>
        var temp: Int
        var amountReceived: Long
        var size: Long
        val buffer = ByteArray(1024 * 64)
        var i = 0

        while (!exit) {
            i++
            fileSize.postValue(0L)
            amountReceived = 0
            val fileInfo = History()
            println("TAAAG ReceiverThreadNo:$i")
            try {
                `is` = socket.getInputStream()
                dis = DataInputStream(`is`)
                info = dis.readUTF().split(":").toTypedArray()
                size = info[0].toLong()
                fileSize.postValue(size)

                socketStatus.postValue("receiving")

                location = getFilePath(info[1], size)
                if (location == "false") {
                    socketStatus.postValue("Storage Is Not Available") //TODO handle it
                    continue
                }
                fos = FileOutputStream(location)

                status.postValue(info[1])
                bytes.postValue(amountReceived)

                fileInfo.apply {
                    fileName = info[1]
                    fileSize = sizeInWord(this@ConnectedViewModel.fileSize.value!!)
                    fileType = getFileType(info[1])
                    fileLocation = location
                    senderName = peerName.value!!
                    receiverName = "self"
                }

                val startTime = System.currentTimeMillis()

                //Receiving Partial Data
                temp = `is`.read(buffer, 0, (size % buffer.size).toInt())
                fos.write(buffer, 0, temp)
                size -= temp.toLong()
                amountReceived += temp.toLong()
                bytes.postValue(amountReceived)

                //Receiving Remaining Data
                while (size > 0) {
                    temp = `is`.read(buffer)
                    fos.write(buffer, 0, temp)
                    size -= temp.toLong()
                    amountReceived += temp.toLong()
                    bytes.postValue(amountReceived)
                    elapsedTime.postValue(System.currentTimeMillis() - startTime)
                }

                println("TAAAG Time:" + elapsedTime.value)
                status.postValue("Receive Complete:" + info[1])
                fileInfo.status = "Success"
                socketStatus.postValue("free")

                fos.flush()
                fos.close()
            } catch (e: Exception) {
                socketStatus.postValue("receivingThread failed")
                if (fileInfo.fileName != "")
                    fileInfo.status = "failure"
                e.printStackTrace()
                stopReceiver()
            } finally {
                if (isConnected()){
                    //socketStatus.postValue("restart Receiver")
                    //receiveFile()
                }
                if (fileInfo.status != "")
                    viewModelScope.launch {
                        fileInfo.dateReceived = Date(System.currentTimeMillis())
                        repository.insertFile(fileInfo)
                    }
            }
        }
    }

    private fun getFilePath(fileName: String, fileSize: Long): String {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly() || !isSpaceAvailable(
                fileSize
            )
        ) {
            return "false"
        }

        val location =
            Environment.getExternalStorageDirectory().absolutePath + "/SendLite/" + getFileType(
                fileName
            )
        val root = File(location)
        if (!root.exists()) {
            root.mkdirs()
        }
        var rootFile = File(location + fileName)
        var i = 0
        while (rootFile.exists()) {
            i++
            rootFile = File("$location$i)$fileName")
        }
        return rootFile.absolutePath
    }


    fun stopReceiver() {
        exit = true
    }

    private fun isSpaceAvailable(fileSize: Long): Boolean {
        val freeSpace = Environment.getExternalStorageDirectory().freeSpace
        println("TAAAG FreeSpace:" + freeSpace / 1024 / 1024)
        return freeSpace - fileSize > 20 * 1024 * 1024
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
    }

    private fun getFileType(name: String): String {
        val mimeType = URLConnection.guessContentTypeFromName(name)
        return if (mimeType != null && !mimeType.contains("*")) {
            mimeType.substring(0, mimeType.indexOf("/")) + "/"
        } else "others/"
    }

    fun calculatePercentage(fileSize: Long, received: Long) =
        ((received.toFloat() / fileSize) * 100).toInt()

    fun sizeInWord(size: Long): String {
        return when {
            size < 1024 -> size.toString() + "Bytes"
            size < 1024 * 1024 -> "%.2f".format((size / (1024f))) + "KB"
            size < 1024 * 1024 * 1024 -> "%.2f".format((size / (1024 * 1024f))) + "MB"
            else -> "%.2f".format(size / (1024 * 1024 * 1024f)) + "GB"
        }
    }

    fun calculateAverageSpeed(elapsedTime: Long, downloadBytes: Long) =
        downloadBytes * 1000 / elapsedTime

    fun remainingTime(elapsedTime: Long, totalBytes: Long, averageSpeed: Long) =
        totalBytes / averageSpeed - elapsedTime / 1000

    fun timeInWords(time: Long): String {
        return when {
            time < 60 -> (time).toString() + "sec"
            time < 60 * 60 -> "%.1f".format((time / (60f))) + "min"
            else -> "%.2f".format(time / (60 * 60f)) + "hour"
        }
    }
}