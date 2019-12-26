package com.s.sendlite

import android.app.Application
import com.s.sendlite.dataClass.LocalDatabase
import com.s.sendlite.dataClass.Repository
import com.s.sendlite.ui.MainModelFactory
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

        //ViewModelFactory
        bind() from provider { MainModelFactory(instance()) }
    }
}