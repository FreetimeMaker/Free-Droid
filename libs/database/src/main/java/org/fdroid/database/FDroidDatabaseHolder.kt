package org.fdroid.database

import android.content.Context
import androidx.room.Room

 public object FDroidDatabaseHolder {
    
     public fun getDb(context: Context): FDroidDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FDroidDatabase::class.java,
            "fdroid_database"
        ).build()
    }
    
     public fun getDb(context: Context, name: String): FDroidDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FDroidDatabase::class.java,
            name
        ).build()
    }
    
     public fun getDb(context: Context, name: String, fixture: FDroidFixture): FDroidDatabase {
        val database = Room.databaseBuilder(
            context.applicationContext,
            FDroidDatabase::class.java,
            name
        ).build()
        
        fixture.prePopulateDb(database)
        return database
    }
}
