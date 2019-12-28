package com.s.sendlite.ui.ConnectedFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.s.sendlite.R
import com.s.sendlite.socket.ReceiverThread
import com.s.sendlite.socket.SenderThread
import kotlinx.android.synthetic.main.connected_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.net.ServerSocket
import java.net.Socket

class ConnectedFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: ConnectedModelFactory by instance()
    private val args: ConnectedFragmentArgs by navArgs()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConnectedViewModel::class.java)
    }

    private lateinit var socket: Socket
    private var fileURI: Uri? = null
    private lateinit var receiver: ReceiverThread

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.connected_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!

        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                connected_text.text = sharedPref.getString(
                    "DeviceName",
                    "NoName"
                ) + "--" + sharedPref.getString("ConnectedTo", "None")
            }

            if (args.hostAddress == "server") {
                server()
            } else {
                client(args.hostAddress)
            }
        }

        btn_send.setOnClickListener {
            if (fileURI != null) {
                sendFile(fileURI!!)
                fileURI = null
            } else {
                Toast.makeText(context, "NO File Selected", Toast.LENGTH_SHORT).show()
            }
        }

        btn_choose.setOnClickListener {
            chooseFile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disableWifi(this.activity!!.application)
        receiver.stopThread()
    }

    private fun chooseFile() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "select file"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            fileURI = data?.data!!
        }
    }

    private fun receiverThreadObserver() {
        receiver = ReceiverThread(socket, context)
        receiver.start()
        receiver.bytesReceived.observe(this, Observer {
            bytes_text.text = it.toString()
            size_text.text = viewModel.sizeReceived(it)
            if (receiver.fileSize != 0L) {
                viewModel.calculatePercentage(receiver.fileSize, it).run {
                    percentage_text.text = this.toString()
                    progress_bar.progress = this
                }
            }
        })

        receiver.status.observe(this, Observer {
            status_text.text = it
            if(it.contains("Complete"))
                progress_bar.progress = 100
        })
    }

    fun sendFile(fileURI: Uri) {
        senderThreadObserver(fileURI)
    }

    private fun senderThreadObserver(fileURI: Uri) {
        val senderThread = SenderThread(context, socket, fileURI)
        senderThread.start()
        senderThread.byteSent.observe(this, Observer {
            bytes_text.text = it.toString()
            size_text.text = viewModel.sizeReceived(it)
            if (senderThread.fileSize != 0L) {
                viewModel.calculatePercentage(senderThread.fileSize, it).run {
                    percentage_text.text = this.toString()
                    progress_bar.progress = this
                }
            }
        })

        senderThread.status.observe(this, Observer {
            status_text.text = it
            if(it.contains("Complete"))
                progress_bar.progress = 100
        })
    }

    suspend fun server() {
        val server = ServerSocket(12327)
        socket = server.accept()
        withContext(Dispatchers.Main) {
            receiverThreadObserver()
        }
    }

    suspend fun client(hostAddress: String) {
        socket = Socket(hostAddress, 12327)
        withContext(Dispatchers.Main) {
            receiverThreadObserver()
        }
    }
}