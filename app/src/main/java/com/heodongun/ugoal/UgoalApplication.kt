package com.heodongun.ugoal

import android.app.Application
import com.heodongun.ugoal.data.remote.MongoDbClient
import com.heodongun.ugoal.data.repository.UgoalRepository

class UgoalApplication : Application() {
    
    lateinit var repository: UgoalRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        val mongoClient = MongoDbClient()
        repository = UgoalRepository(mongoClient)
    }
}
