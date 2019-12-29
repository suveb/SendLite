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
import com.s.sendlite.R
import com.s.sendlite.WifiDirectMethodsImpl
import kotlinx.android.synthetic.main.connected_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class ConnectedFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val broadcastReceiver: WifiDirectMethodsImpl by instance()
    private val viewModelFactory: ConnectedModelFactory by instance()
    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(ConnectedViewModel::class.java)
    }

    private var fileURI: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.connected_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!

        viewModel.peerName.observe(this, Observer {
            connection_status.text = sharedPref.getString("DeviceName", "")!! + it
        })

        viewModel.initialise(
            broadcastReceiver.memberType.value!!,
            broadcastReceiver.hostAddress.value!!,
            sharedPref.getString("DeviceName", "")!!
        )

        viewModel.bytes.observe(this, Observer {
            bytes_text.text = it.toString()
            size_text.text = viewModel.sizeReceived(it)
            if (viewModel.fileSize != 0L) {
                viewModel.calculatePercentage(viewModel.fileSize, it).run {
                    percentage_text.text = this.toString()
                    progress_bar.progress = this
                }
            }
        })

        viewModel.status.observe(this, Observer {
            status_text.text = it
            if (it.contains("Complete")) {
                percentage_text.text = "100"
                progress_bar.progress = 100
            }
        })

        btn_send.setOnClickListener {
            if (fileURI != null) {
                viewModel.sendFile(this@ConnectedFragment.context!!, fileURI!!)
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
        viewModel.stopReceiver()
        broadcastReceiver.turnWifiOff()
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
}