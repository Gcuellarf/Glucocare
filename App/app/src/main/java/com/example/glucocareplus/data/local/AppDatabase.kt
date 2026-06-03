package com.example.glucocareplus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MedicamentoEntity::class, GlucemiaEntity::class, NovedadEntity::class, CitaEntity::class, UsuarioEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun glucemiaDao(): GlucemiaDao
    abstract fun novedadDao(): NovedadDao
    abstract fun citaDao(): CitaDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glucocareplus_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}