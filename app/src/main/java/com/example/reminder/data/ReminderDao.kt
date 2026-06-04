package com.example.reminder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ReminderDao {
    @Query("DELETE FROM scheduled_reminders WHERE scheduledTime < :now AND isCompleted = 0")
    suspend fun deleteOldUncompletedReminders(now: LocalDateTime)

    @Transaction
    @Query("SELECT * FROM reminder_policies")
    fun getAllPoliciesWithNextReminder(): Flow<List<PolicyWithNextReminder>>

    @Query("SELECT * FROM scheduled_reminders WHERE (scheduledTime < :now AND isCompleted = 0 AND isActivated = 0) OR (isActivated = 1 AND activationTime < :expiredLimit)")
    suspend fun getOldUncompletedReminders(now: LocalDateTime, expiredLimit: LocalDateTime): List<ScheduledReminder>

    @Query("UPDATE scheduled_reminders SET isActivated = 1, activationTime = :time WHERE id = :id")
    suspend fun markActivated(id: Int, time: LocalDateTime)

    @Query("UPDATE scheduled_reminders SET isActivated = 0 WHERE id = :id")
    suspend fun markDeactivated(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: ReminderPolicy): Long

    @Update
    suspend fun updatePolicy(policy: ReminderPolicy)

    @Query("SELECT * FROM scheduled_reminders WHERE isCompleted = 0 ORDER BY scheduledTime ASC")
    fun getPendingReminders(): Flow<List<ScheduledReminder>>

    @Insert
    suspend fun insertScheduledReminder(reminder: ScheduledReminder): Long

    @Update
    suspend fun updateScheduledReminder(reminder: ScheduledReminder)

    @Query("SELECT * FROM reminder_policies WHERE id = :id")
    suspend fun getPolicyById(id: Int): ReminderPolicy?

    @Query("DELETE FROM reminder_policies WHERE id = :id")
    suspend fun deletePolicy(id: Int)

    @Query("DELETE FROM scheduled_reminders WHERE policyId = :policyId")
    suspend fun deleteScheduledRemindersByPolicy(policyId: Int)

    @Query("SELECT * FROM scheduled_reminders WHERE policyId = :policyId AND isCompleted = 0 ORDER BY scheduledTime ASC LIMIT 1")
    suspend fun getNextScheduledReminder(policyId: Int): ScheduledReminder?

    @Query("DELETE FROM scheduled_reminders WHERE id = :id")
    suspend fun deleteScheduledReminder(id: Int)

    @Query("DELETE FROM scheduled_reminders WHERE policyId = :policyId AND isCompleted = 0")
    suspend fun deleteAllPendingRemindersForPolicy(policyId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM scheduled_reminders WHERE scheduledTime = :time AND isCompleted = 0 LIMIT 1)")
    suspend fun hasReminderAtTime(time: LocalDateTime): Boolean
}
