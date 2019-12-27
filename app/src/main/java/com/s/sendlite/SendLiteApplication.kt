package com.s.sendlite

import android.app.Application
import com.s.sendlite.dataClass.LocalDatabase
import com.s.sendlite.dataClass.Repository
import com.s.sendlite.ui.AvailableDeviceFragment.AvailableDeviceModelFactory
import com.s.sendlite.ui.ConnectedFragment.ConnectedModelFactory
import com.s.sendlite.ui.DashboardFragment.DashboardModelFactory
import com.s.sendlite.ui.DeviceNameFragment.DeviceNameModelFactory
import com.s.sendlite.ui.HistoryFragment.HistoryModelFactory
import com.s.sendlite.ui.SettingsFragment.SettingsModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class SendLiteApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        import(androidXModule(this@SendLiteApplication))

        //Local
        bind() from singleton { LocalDatabase(instance()) }
        bind() from singleton { instance<LocalDatabase>().query() }

        //Repository
        bind() from singleton { Repository(instance()) }

        //BroadcastReceiver
        bind() from singleton { WifiDirectBroadcastReceiver() }

        //ViewModelFactory
        bind() from provider { AvailableDeviceModelFactory(instance()) }
        bind() from provider { ConnectedModelFactory(instance()) }
        bind() from provider { DashboardModelFactory(instance()) }
        bind() from provider { DeviceNameModelFactory(instance()) }
        bind() from provider { HistoryModelFactory(instance()) }
        bind() from provider { SettingsModelFactory(instance()) }

    }
}