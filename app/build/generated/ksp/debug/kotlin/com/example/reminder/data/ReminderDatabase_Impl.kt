package com.example.reminder.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ReminderDatabase_Impl : ReminderDatabase() {
  private val _reminderDao: Lazy<ReminderDao> = lazy {
    ReminderDao_Impl(this)
  }

  private val _logDao: Lazy<LogDao> = lazy {
    LogDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(12, "daca078f1156972d9674f7f0a7c1f65a", "8b2eb61a73161e4a794c3110f70fcf73") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `reminder_policies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `message` TEXT NOT NULL, `startDate` TEXT NOT NULL, `period` TEXT NOT NULL, `type` TEXT NOT NULL, `nagIntervalMinutes` INTEGER, `customRingtoneUri` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `scheduled_reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `policyId` INTEGER NOT NULL, `scheduledTime` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `isActivated` INTEGER NOT NULL, `activationTime` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `event_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` TEXT NOT NULL, `eventType` TEXT NOT NULL, `policyTitle` TEXT NOT NULL, `details` TEXT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'daca078f1156972d9674f7f0a7c1f65a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `reminder_policies`")
        connection.execSQL("DROP TABLE IF EXISTS `scheduled_reminders`")
        connection.execSQL("DROP TABLE IF EXISTS `event_logs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsReminderPolicies: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsReminderPolicies.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("message", TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("startDate", TableInfo.Column("startDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("period", TableInfo.Column("period", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("nagIntervalMinutes", TableInfo.Column("nagIntervalMinutes", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsReminderPolicies.put("customRingtoneUri", TableInfo.Column("customRingtoneUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysReminderPolicies: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesReminderPolicies: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoReminderPolicies: TableInfo = TableInfo("reminder_policies", _columnsReminderPolicies, _foreignKeysReminderPolicies, _indicesReminderPolicies)
        val _existingReminderPolicies: TableInfo = read(connection, "reminder_policies")
        if (!_infoReminderPolicies.equals(_existingReminderPolicies)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |reminder_policies(com.example.reminder.data.ReminderPolicy).
              | Expected:
              |""".trimMargin() + _infoReminderPolicies + """
              |
              | Found:
              |""".trimMargin() + _existingReminderPolicies)
        }
        val _columnsScheduledReminders: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsScheduledReminders.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScheduledReminders.put("policyId", TableInfo.Column("policyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScheduledReminders.put("scheduledTime", TableInfo.Column("scheduledTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScheduledReminders.put("isCompleted", TableInfo.Column("isCompleted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScheduledReminders.put("isActivated", TableInfo.Column("isActivated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsScheduledReminders.put("activationTime", TableInfo.Column("activationTime", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysScheduledReminders: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesScheduledReminders: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoScheduledReminders: TableInfo = TableInfo("scheduled_reminders", _columnsScheduledReminders, _foreignKeysScheduledReminders, _indicesScheduledReminders)
        val _existingScheduledReminders: TableInfo = read(connection, "scheduled_reminders")
        if (!_infoScheduledReminders.equals(_existingScheduledReminders)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |scheduled_reminders(com.example.reminder.data.ScheduledReminder).
              | Expected:
              |""".trimMargin() + _infoScheduledReminders + """
              |
              | Found:
              |""".trimMargin() + _existingScheduledReminders)
        }
        val _columnsEventLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsEventLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEventLogs.put("timestamp", TableInfo.Column("timestamp", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEventLogs.put("eventType", TableInfo.Column("eventType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEventLogs.put("policyTitle", TableInfo.Column("policyTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsEventLogs.put("details", TableInfo.Column("details", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysEventLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesEventLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoEventLogs: TableInfo = TableInfo("event_logs", _columnsEventLogs, _foreignKeysEventLogs, _indicesEventLogs)
        val _existingEventLogs: TableInfo = read(connection, "event_logs")
        if (!_infoEventLogs.equals(_existingEventLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |event_logs(com.example.reminder.data.LogEntry).
              | Expected:
              |""".trimMargin() + _infoEventLogs + """
              |
              | Found:
              |""".trimMargin() + _existingEventLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "reminder_policies", "scheduled_reminders", "event_logs")
  }

  public override fun clearAllTables() {
    super.performClear(false, "reminder_policies", "scheduled_reminders", "event_logs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ReminderDao::class, ReminderDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LogDao::class, LogDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun reminderDao(): ReminderDao = _reminderDao.value

  public override fun logDao(): LogDao = _logDao.value
}
