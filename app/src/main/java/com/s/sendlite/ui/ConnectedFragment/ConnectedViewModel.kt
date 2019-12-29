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
    var fileSize = 0L

    private lateinit var socket: Socket

    @Volatile
    private var exit = false

    fun calculatePercentage(fileSize: Long, received: Long) =
        ((received.toFloat() / fileSize) * 100).toInt()

    fun initialise(memberType: String, hostAddress: String, myName: String) = Thread {
        try {
            if (memberType == "Server") {
                initServer(myName)
            } else if (memberType == "Client") {
                initClient(hostAddress, myName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            status.postValue("Connection Not Established")
        }
    }.start()

    private fun initServer(myName: String) {
        val server = ServerSocket(12327)
        socket = server.accept()

        //Sending Server Name
        DataOutputStream(socket.getOutputStream()).writeUTF(myName)
        //Receiving Client Name
        peerName.postValue(DataInputStream(socket.getInputStream()).readUTF())

        receiveFile()
    }

    private fun initClient(hostAddress: String, myName: String) {
        socket = Socket(hostAddress, 12327)

        //Receiving Server Name
        peerName.postValue(DataInputStream(socket.getInputStream()).readUTF())
        //Sending Client Name
        DataOutputStream(socket.getOutputStream()).writeUTF(myName)

        receiveFile()
    }

    fun stopReceiver() {
        exit = true
    }

    fun sendFile(context: Context, fileUri: Uri) = Thread {
        val fileInfo = History()
        try {
            val buffer = ByteArray(1024 * 64)
            val cursor = context.contentResolver.query(fileUri, null, null, null, null)
            cursor?.moveToFirst()
            val name = cursor?.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))!!
            fileSize = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE)).toLong()

            val `is` = context.contentResolver.openInputStream(fileUri)!!
            val os = socket.getOutputStream()!!
            val dos = DataOutputStream(os)

            var temp: Int
            var amountSent = 0L

            dos.writeUTF("$fileSize:$name")
            status.postValue("Sending:$name:$fileSize")
            bytes.postValue(amountSent)

            fileInfo.apply {
                fileName = name
                fileSize = sizeReceived(this@ConnectedViewModel.fileSize)
                fileType = getFileType(name)
                fileLocation = fileUri.path!!
                senderName = "self"
                receiverName = peerName.value!!
                dateReceived = Date(System.currentTimeMillis())
            }

            //Sending Partial Data
            temp = `is`.read(buffer, 0, (fileSize % buffer.size).toInt())
            os.write(buffer, 0, temp)
            amountSent += temp.toLong()
            bytes.postValue(amountSent)

            //Sending Remaining Data
            while (`is`.read(buffer).also { temp = it } != -1) {
                os.write(buffer)
                amountSent += temp.toLong()
                bytes.postValue(amountSent)
            }

            status.postValue("Send Complete:$name:$fileSize")
            fileInfo.status = "Success"

            os.flush()
            dos.flush()
            `is`.close()
            cursor.close()
        } catch (e: IOException) {
            fileSize = 0
            fileInfo.status = "Failure"
            status.postValue("UnExpectedError")
            e.printStackTrace()
        } finally {
            if (fileInfo.status != "")
                viewModelScope.launch {
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
            fileSize = 0L
            amountReceived = 0
            val fileInfo = History()
            println("TAAAG ReceiverThreadNo:$i")
            try {
                `is` = socket.getInputStream()
                dis = DataInputStream(`is`)
                info = dis.readUTF().split(":").toTypedArray()
                size = info[0].toLong()
                fileSize = size
                location = getFilePath(info[1], size)
                if (location == "false") {
                    status.postValue("Storage Is Not Available")
                    continue
                }
                println("TAAAG location:$location")
                fos = FileOutputStream(location)

                status.postValue("Receiving:" + info[1])
                bytes.postValue(amountReceived)

                val s = System.currentTimeMillis()

                fileInfo.apply {
                    fileName = info[1]
                    fileSize = sizeReceived(this@ConnectedViewModel.fileSize)
                    fileType = getFileType(info[1])
                    fileLocation = location
                    senderName = peerName.value!!
                    receiverName = "self"
                    dateReceived = Date(s)
                }

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
                }

                println("TAAAG Time:" + (System.currentTimeMillis() - s))
                status.postValue("Receive Complete:" + info[1])
                fileInfo.status = "Success"

                fos.flush()
                fos.close()
            } catch (e: Exception) {
                if (fileInfo.fileName != "")
                    fileInfo.status = "failure"
                e.printStackTrace()
                stopReceiver()
            } finally {
                if (fileInfo.status != "")
                    viewModelScope.launch {
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

    private fun isSpaceAvailable(fileSize: Long): Boolean {
        val freeSpace = Environment.getExternalStorageDirectory().freeSpace
        println("TAAAG FreeSpace:" + freeSpace / 1024 / 1024)
        return freeSpace - fileSize > 30 * 1024 * 1024
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

    fun sizeReceived(size: Long): String {
        return when {
            size < 1024 -> size.toString() + "Bytes"
            size < 1024 * 1024 -> "%.2f".format((size / (1024f))) + "KB"
            else -> "%.2f".format(size / (1024 * 1024f)) + "MB"
        }
    }
}