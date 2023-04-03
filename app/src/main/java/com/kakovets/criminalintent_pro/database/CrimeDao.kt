package com.kakovets.criminalintent_pro.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kakovets.criminalintent_pro.Crime
import java.util.UUID

@Dao
interface CrimeDao {

    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id = :id")
    fun getCrime(id: UUID): LiveData<Crime?>

    @Query("DELETE FROM crime")
    fun dropDatabase()

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)
}