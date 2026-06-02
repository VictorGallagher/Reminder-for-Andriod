package com.example.reminder.`data`

import androidx.collection.LongSparseArray
import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndex
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.room.util.recursiveFetchLongSparseArray
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import java.time.LocalDateTime
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReminderDao_Impl(
  __db: RoomDatabase,
) : ReminderDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfReminderPolicy: EntityInsertAdapter<ReminderPolicy>

  private val __converters: Converters = Converters()

  private val __insertAdapterOfScheduledReminder: EntityInsertAdapter<ScheduledReminder>

  private val __updateAdapterOfReminderPolicy: EntityDeleteOrUpdateAdapter<ReminderPolicy>

  private val __updateAdapterOfScheduledReminder: EntityDeleteOrUpdateAdapter<ScheduledReminder>
  init {
    this.__db = __db
    this.__insertAdapterOfReminderPolicy = object : EntityInsertAdapter<ReminderPolicy>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `reminder_policies` (`id`,`title`,`message`,`startDate`,`period`,`type`,`nagIntervalMinutes`,`customRingtoneUri`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderPolicy) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.message)
        val _tmp: String? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmp)
        }
        val _tmp_1: String = __converters.toPeriod(entity.period)
        statement.bindText(5, _tmp_1)
        val _tmp_2: String = __converters.toType(entity.type)
        statement.bindText(6, _tmp_2)
        val _tmpNagIntervalMinutes: Int? = entity.nagIntervalMinutes
        if (_tmpNagIntervalMinutes == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpNagIntervalMinutes.toLong())
        }
        val _tmpCustomRingtoneUri: String? = entity.customRingtoneUri
        if (_tmpCustomRingtoneUri == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpCustomRingtoneUri)
        }
      }
    }
    this.__insertAdapterOfScheduledReminder = object : EntityInsertAdapter<ScheduledReminder>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `scheduled_reminders` (`id`,`policyId`,`scheduledTime`,`isCompleted`,`isActivated`,`activationTime`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ScheduledReminder) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.policyId.toLong())
        val _tmp: String? = __converters.dateToTimestamp(entity.scheduledTime)
        if (_tmp == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmp)
        }
        val _tmp_1: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(4, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isActivated) 1 else 0
        statement.bindLong(5, _tmp_2.toLong())
        val _tmpActivationTime: LocalDateTime? = entity.activationTime
        val _tmp_3: String? = __converters.dateToTimestamp(_tmpActivationTime)
        if (_tmp_3 == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp_3)
        }
      }
    }
    this.__updateAdapterOfReminderPolicy = object : EntityDeleteOrUpdateAdapter<ReminderPolicy>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `reminder_policies` SET `id` = ?,`title` = ?,`message` = ?,`startDate` = ?,`period` = ?,`type` = ?,`nagIntervalMinutes` = ?,`customRingtoneUri` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ReminderPolicy) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.message)
        val _tmp: String? = __converters.dateToTimestamp(entity.startDate)
        if (_tmp == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmp)
        }
        val _tmp_1: String = __converters.toPeriod(entity.period)
        statement.bindText(5, _tmp_1)
        val _tmp_2: String = __converters.toType(entity.type)
        statement.bindText(6, _tmp_2)
        val _tmpNagIntervalMinutes: Int? = entity.nagIntervalMinutes
        if (_tmpNagIntervalMinutes == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpNagIntervalMinutes.toLong())
        }
        val _tmpCustomRingtoneUri: String? = entity.customRingtoneUri
        if (_tmpCustomRingtoneUri == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpCustomRingtoneUri)
        }
        statement.bindLong(9, entity.id.toLong())
      }
    }
    this.__updateAdapterOfScheduledReminder = object : EntityDeleteOrUpdateAdapter<ScheduledReminder>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `scheduled_reminders` SET `id` = ?,`policyId` = ?,`scheduledTime` = ?,`isCompleted` = ?,`isActivated` = ?,`activationTime` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ScheduledReminder) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.policyId.toLong())
        val _tmp: String? = __converters.dateToTimestamp(entity.scheduledTime)
        if (_tmp == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmp)
        }
        val _tmp_1: Int = if (entity.isCompleted) 1 else 0
        statement.bindLong(4, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isActivated) 1 else 0
        statement.bindLong(5, _tmp_2.toLong())
        val _tmpActivationTime: LocalDateTime? = entity.activationTime
        val _tmp_3: String? = __converters.dateToTimestamp(_tmpActivationTime)
        if (_tmp_3 == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmp_3)
        }
        statement.bindLong(7, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertPolicy(policy: ReminderPolicy): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfReminderPolicy.insertAndReturnId(_connection, policy)
    _result
  }

  public override suspend fun insertScheduledReminder(reminder: ScheduledReminder): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfScheduledReminder.insertAndReturnId(_connection, reminder)
    _result
  }

  public override suspend fun updatePolicy(policy: ReminderPolicy): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfReminderPolicy.handle(_connection, policy)
  }

  public override suspend fun updateScheduledReminder(reminder: ScheduledReminder): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfScheduledReminder.handle(_connection, reminder)
  }

  public override fun getAllPoliciesWithNextReminder(): Flow<List<PolicyWithNextReminder>> {
    val _sql: String = "SELECT * FROM reminder_policies"
    return createFlow(__db, true, arrayOf("scheduled_reminders", "reminder_policies")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfMessage: Int = getColumnIndexOrThrow(_stmt, "message")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt, "nagIntervalMinutes")
        val _columnIndexOfCustomRingtoneUri: Int = getColumnIndexOrThrow(_stmt, "customRingtoneUri")
        val _collectionScheduledReminders: LongSparseArray<MutableList<ScheduledReminder>> = LongSparseArray<MutableList<ScheduledReminder>>()
        while (_stmt.step()) {
          val _tmpKey: Long
          _tmpKey = _stmt.getLong(_columnIndexOfId)
          if (!_collectionScheduledReminders.containsKey(_tmpKey)) {
            _collectionScheduledReminders.put(_tmpKey, mutableListOf())
          }
        }
        _stmt.reset()
        __fetchRelationshipscheduledRemindersAscomExampleReminderDataScheduledReminder(_connection, _collectionScheduledReminders)
        val _result: MutableList<PolicyWithNextReminder> = mutableListOf()
        while (_stmt.step()) {
          val _item: PolicyWithNextReminder
          val _tmpPolicy: ReminderPolicy
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpMessage: String
          _tmpMessage = _stmt.getText(_columnIndexOfMessage)
          val _tmpStartDate: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpPeriod: Period
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfPeriod)
          _tmpPeriod = __converters.fromPeriod(_tmp_2)
          val _tmpType: ReminderType
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.fromType(_tmp_3)
          val _tmpNagIntervalMinutes: Int?
          if (_stmt.isNull(_columnIndexOfNagIntervalMinutes)) {
            _tmpNagIntervalMinutes = null
          } else {
            _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          }
          val _tmpCustomRingtoneUri: String?
          if (_stmt.isNull(_columnIndexOfCustomRingtoneUri)) {
            _tmpCustomRingtoneUri = null
          } else {
            _tmpCustomRingtoneUri = _stmt.getText(_columnIndexOfCustomRingtoneUri)
          }
          _tmpPolicy = ReminderPolicy(_tmpId,_tmpTitle,_tmpMessage,_tmpStartDate,_tmpPeriod,_tmpType,_tmpNagIntervalMinutes,_tmpCustomRingtoneUri)
          val _tmpScheduledRemindersCollection: MutableList<ScheduledReminder>
          val _tmpKey_1: Long
          _tmpKey_1 = _stmt.getLong(_columnIndexOfId)
          _tmpScheduledRemindersCollection = checkNotNull(_collectionScheduledReminders.get(_tmpKey_1))
          _item = PolicyWithNextReminder(_tmpPolicy,_tmpScheduledRemindersCollection)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getOldUncompletedReminders(now: LocalDateTime, expiredLimit: LocalDateTime): List<ScheduledReminder> {
    val _sql: String = "SELECT * FROM scheduled_reminders WHERE (scheduledTime < ? AND isCompleted = 0 AND isActivated = 0) OR (isActivated = 1 AND activationTime < ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.dateToTimestamp(now)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        _argIndex = 2
        val _tmp_1: String? = __converters.dateToTimestamp(expiredLimit)
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp_1)
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPolicyId: Int = getColumnIndexOrThrow(_stmt, "policyId")
        val _columnIndexOfScheduledTime: Int = getColumnIndexOrThrow(_stmt, "scheduledTime")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActivated: Int = getColumnIndexOrThrow(_stmt, "isActivated")
        val _columnIndexOfActivationTime: Int = getColumnIndexOrThrow(_stmt, "activationTime")
        val _result: MutableList<ScheduledReminder> = mutableListOf()
        while (_stmt.step()) {
          val _item: ScheduledReminder
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPolicyId: Int
          _tmpPolicyId = _stmt.getLong(_columnIndexOfPolicyId).toInt()
          val _tmpScheduledTime: LocalDateTime
          val _tmp_2: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmp_2 = null
          } else {
            _tmp_2 = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmp_3: LocalDateTime? = __converters.fromTimestamp(_tmp_2)
          if (_tmp_3 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpScheduledTime = _tmp_3
          }
          val _tmpIsCompleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_4 != 0
          val _tmpIsActivated: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfIsActivated).toInt()
          _tmpIsActivated = _tmp_5 != 0
          val _tmpActivationTime: LocalDateTime?
          val _tmp_6: String?
          if (_stmt.isNull(_columnIndexOfActivationTime)) {
            _tmp_6 = null
          } else {
            _tmp_6 = _stmt.getText(_columnIndexOfActivationTime)
          }
          _tmpActivationTime = __converters.fromTimestamp(_tmp_6)
          _item = ScheduledReminder(_tmpId,_tmpPolicyId,_tmpScheduledTime,_tmpIsCompleted,_tmpIsActivated,_tmpActivationTime)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPendingReminders(): Flow<List<ScheduledReminder>> {
    val _sql: String = "SELECT * FROM scheduled_reminders WHERE isCompleted = 0 ORDER BY scheduledTime ASC"
    return createFlow(__db, false, arrayOf("scheduled_reminders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPolicyId: Int = getColumnIndexOrThrow(_stmt, "policyId")
        val _columnIndexOfScheduledTime: Int = getColumnIndexOrThrow(_stmt, "scheduledTime")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActivated: Int = getColumnIndexOrThrow(_stmt, "isActivated")
        val _columnIndexOfActivationTime: Int = getColumnIndexOrThrow(_stmt, "activationTime")
        val _result: MutableList<ScheduledReminder> = mutableListOf()
        while (_stmt.step()) {
          val _item: ScheduledReminder
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPolicyId: Int
          _tmpPolicyId = _stmt.getLong(_columnIndexOfPolicyId).toInt()
          val _tmpScheduledTime: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpScheduledTime = _tmp_1
          }
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          val _tmpIsActivated: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsActivated).toInt()
          _tmpIsActivated = _tmp_3 != 0
          val _tmpActivationTime: LocalDateTime?
          val _tmp_4: String?
          if (_stmt.isNull(_columnIndexOfActivationTime)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getText(_columnIndexOfActivationTime)
          }
          _tmpActivationTime = __converters.fromTimestamp(_tmp_4)
          _item = ScheduledReminder(_tmpId,_tmpPolicyId,_tmpScheduledTime,_tmpIsCompleted,_tmpIsActivated,_tmpActivationTime)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPolicyById(id: Int): ReminderPolicy? {
    val _sql: String = "SELECT * FROM reminder_policies WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfMessage: Int = getColumnIndexOrThrow(_stmt, "message")
        val _columnIndexOfStartDate: Int = getColumnIndexOrThrow(_stmt, "startDate")
        val _columnIndexOfPeriod: Int = getColumnIndexOrThrow(_stmt, "period")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfNagIntervalMinutes: Int = getColumnIndexOrThrow(_stmt, "nagIntervalMinutes")
        val _columnIndexOfCustomRingtoneUri: Int = getColumnIndexOrThrow(_stmt, "customRingtoneUri")
        val _result: ReminderPolicy?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpMessage: String
          _tmpMessage = _stmt.getText(_columnIndexOfMessage)
          val _tmpStartDate: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfStartDate)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfStartDate)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpStartDate = _tmp_1
          }
          val _tmpPeriod: Period
          val _tmp_2: String
          _tmp_2 = _stmt.getText(_columnIndexOfPeriod)
          _tmpPeriod = __converters.fromPeriod(_tmp_2)
          val _tmpType: ReminderType
          val _tmp_3: String
          _tmp_3 = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.fromType(_tmp_3)
          val _tmpNagIntervalMinutes: Int?
          if (_stmt.isNull(_columnIndexOfNagIntervalMinutes)) {
            _tmpNagIntervalMinutes = null
          } else {
            _tmpNagIntervalMinutes = _stmt.getLong(_columnIndexOfNagIntervalMinutes).toInt()
          }
          val _tmpCustomRingtoneUri: String?
          if (_stmt.isNull(_columnIndexOfCustomRingtoneUri)) {
            _tmpCustomRingtoneUri = null
          } else {
            _tmpCustomRingtoneUri = _stmt.getText(_columnIndexOfCustomRingtoneUri)
          }
          _result = ReminderPolicy(_tmpId,_tmpTitle,_tmpMessage,_tmpStartDate,_tmpPeriod,_tmpType,_tmpNagIntervalMinutes,_tmpCustomRingtoneUri)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getNextScheduledReminder(policyId: Int): ScheduledReminder? {
    val _sql: String = "SELECT * FROM scheduled_reminders WHERE policyId = ? AND isCompleted = 0 ORDER BY scheduledTime ASC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, policyId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPolicyId: Int = getColumnIndexOrThrow(_stmt, "policyId")
        val _columnIndexOfScheduledTime: Int = getColumnIndexOrThrow(_stmt, "scheduledTime")
        val _columnIndexOfIsCompleted: Int = getColumnIndexOrThrow(_stmt, "isCompleted")
        val _columnIndexOfIsActivated: Int = getColumnIndexOrThrow(_stmt, "isActivated")
        val _columnIndexOfActivationTime: Int = getColumnIndexOrThrow(_stmt, "activationTime")
        val _result: ScheduledReminder?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPolicyId: Int
          _tmpPolicyId = _stmt.getLong(_columnIndexOfPolicyId).toInt()
          val _tmpScheduledTime: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpScheduledTime = _tmp_1
          }
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          val _tmpIsActivated: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsActivated).toInt()
          _tmpIsActivated = _tmp_3 != 0
          val _tmpActivationTime: LocalDateTime?
          val _tmp_4: String?
          if (_stmt.isNull(_columnIndexOfActivationTime)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getText(_columnIndexOfActivationTime)
          }
          _tmpActivationTime = __converters.fromTimestamp(_tmp_4)
          _result = ScheduledReminder(_tmpId,_tmpPolicyId,_tmpScheduledTime,_tmpIsCompleted,_tmpIsActivated,_tmpActivationTime)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun hasReminderAtTime(time: LocalDateTime): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM scheduled_reminders WHERE scheduledTime = ? AND isCompleted = 0 LIMIT 1)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.dateToTimestamp(time)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(0).toInt()
          _result = _tmp_1 != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOldUncompletedReminders(now: LocalDateTime) {
    val _sql: String = "DELETE FROM scheduled_reminders WHERE scheduledTime < ? AND isCompleted = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.dateToTimestamp(now)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markActivated(id: Int, time: LocalDateTime) {
    val _sql: String = "UPDATE scheduled_reminders SET isActivated = 1, activationTime = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String? = __converters.dateToTimestamp(time)
        if (_tmp == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, _tmp)
        }
        _argIndex = 2
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markDeactivated(id: Int) {
    val _sql: String = "UPDATE scheduled_reminders SET isActivated = 0 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePolicy(id: Int) {
    val _sql: String = "DELETE FROM reminder_policies WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteScheduledRemindersByPolicy(policyId: Int) {
    val _sql: String = "DELETE FROM scheduled_reminders WHERE policyId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, policyId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteScheduledReminder(id: Int) {
    val _sql: String = "DELETE FROM scheduled_reminders WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __fetchRelationshipscheduledRemindersAscomExampleReminderDataScheduledReminder(_connection: SQLiteConnection, _map: LongSparseArray<MutableList<ScheduledReminder>>) {
    if (_map.isEmpty()) {
      return
    }
    if (_map.size() > 999) {
      recursiveFetchLongSparseArray(_map, true) { _tmpMap ->
        __fetchRelationshipscheduledRemindersAscomExampleReminderDataScheduledReminder(_connection, _tmpMap)
      }
      return
    }
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT `id`,`policyId`,`scheduledTime`,`isCompleted`,`isActivated`,`activationTime` FROM `scheduled_reminders` WHERE `policyId` IN (")
    val _inputSize: Int = _map.size()
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    val _stmt: SQLiteStatement = _connection.prepare(_sql)
    var _argIndex: Int = 1
    for (i in 0 until _map.size()) {
      val _item: Long = _map.keyAt(i)
      _stmt.bindLong(_argIndex, _item)
      _argIndex++
    }
    try {
      val _itemKeyIndex: Int = getColumnIndex(_stmt, "policyId")
      if (_itemKeyIndex == -1) {
        return
      }
      val _columnIndexOfId: Int = 0
      val _columnIndexOfPolicyId: Int = 1
      val _columnIndexOfScheduledTime: Int = 2
      val _columnIndexOfIsCompleted: Int = 3
      val _columnIndexOfIsActivated: Int = 4
      val _columnIndexOfActivationTime: Int = 5
      while (_stmt.step()) {
        val _tmpKey: Long
        _tmpKey = _stmt.getLong(_itemKeyIndex)
        val _tmpRelation: MutableList<ScheduledReminder>? = _map.get(_tmpKey)
        if (_tmpRelation != null) {
          val _item_1: ScheduledReminder
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpPolicyId: Int
          _tmpPolicyId = _stmt.getLong(_columnIndexOfPolicyId).toInt()
          val _tmpScheduledTime: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfScheduledTime)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfScheduledTime)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpScheduledTime = _tmp_1
          }
          val _tmpIsCompleted: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsCompleted).toInt()
          _tmpIsCompleted = _tmp_2 != 0
          val _tmpIsActivated: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsActivated).toInt()
          _tmpIsActivated = _tmp_3 != 0
          val _tmpActivationTime: LocalDateTime?
          val _tmp_4: String?
          if (_stmt.isNull(_columnIndexOfActivationTime)) {
            _tmp_4 = null
          } else {
            _tmp_4 = _stmt.getText(_columnIndexOfActivationTime)
          }
          _tmpActivationTime = __converters.fromTimestamp(_tmp_4)
          _item_1 = ScheduledReminder(_tmpId,_tmpPolicyId,_tmpScheduledTime,_tmpIsCompleted,_tmpIsActivated,_tmpActivationTime)
          _tmpRelation.add(_item_1)
        }
      }
    } finally {
      _stmt.close()
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
