package com.example.fakeartistny

import android.app.Application
import com.example.fakeartistny.data.PlayerDatabase

class BaseApplication : Application() {
    val database: PlayerDatabase by lazy { PlayerDatabase.getDatabase(this) }
}