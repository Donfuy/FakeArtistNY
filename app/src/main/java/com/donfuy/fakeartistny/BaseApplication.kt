package com.donfuy.fakeartistny

import android.app.Application
import com.donfuy.fakeartistny.data.PlayerDatabase

class BaseApplication : Application() {
    val database: PlayerDatabase by lazy { PlayerDatabase.getDatabase(this) }
}