package com.example.reminder.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import java.time.LocalDateTime
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LogDao_Impl(
  __db: RoomDatabase,
) : LogDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLogEntry: EntityInsertAdapter<LogEntry>

  private val __converters: Converters = Converters()
  init {
    this.__db = __db
    this.__insertAdapterOfLogEntry = object : EntityInsertAdapter<LogEntry>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `event_logs` (`id`,`timestamp`,`eventType`,`policyTitle`,`details`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LogEntry) {
        statement.bindLong(1, entity.id.toLong())
        val _tmp: String? = __converters.dateToTimestamp(entity.timestamp)
        if (_tmp == null) {
          statement.bindNull(2)
        } else {
          statement.bindText(2, _tmp)
        }
        statement.bindText(3, entity.eventType)
        statement.bindText(4, entity.policyTitle)
        val _tmpDetails: String? = entity.details
        if (_tmpDetails == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpDetails)
        }
      }
    }
  }

  public override suspend fun insertLog(log: LogEntry): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLogEntry.insert(_connection, log)
  }

  public override fun getAllLogs(): Flow<List<LogEntry>> {
    val _sql: String = "SELECT * FROM event_logs ORDER BY timestamp DESC LIMIT 500"
    return createFlow(__db, false, arrayOf("event_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfEventType: Int = getColumnIndexOrThrow(_stmt, "eventType")
        val _columnIndexOfPolicyTitle: Int = getColumnIndexOrThrow(_stmt, "policyTitle")
        val _columnIndexOfDetails: Int = getColumnIndexOrThrow(_stmt, "details")
        val _result: MutableList<LogEntry> = mutableListOf()
        while (_stmt.step()) {
          val _item: LogEntry
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTimestamp: LocalDateTime
          val _tmp: String?
          if (_stmt.isNull(_columnIndexOfTimestamp)) {
            _tmp = null
          } else {
            _tmp = _stmt.getText(_columnIndexOfTimestamp)
          }
          val _tmp_1: LocalDateTime? = __converters.fromTimestamp(_tmp)
          if (_tmp_1 == null) {
            error("Expected NON-NULL 'java.time.LocalDateTime', but it was NULL.")
          } else {
            _tmpTimestamp = _tmp_1
          }
          val _tmpEventType: String
          _tmpEventType = _stmt.getText(_columnIndexOfEventType)
          val _tmpPolicyTitle: String
          _tmpPolicyTitle = _stmt.getText(_columnIndexOfPolicyTitle)
          val _tmpDetails: String?
          if (_stmt.isNull(_columnIndexOfDetails)) {
            _tmpDetails = null
          } else {
            _tmpDetails = _stmt.getText(_columnIndexOfDetails)
          }
          _item = LogEntry(_tmpId,_tmpTimestamp,_tmpEventType,_tmpPolicyTitle,_tmpDetails)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearLogs() {
    val _sql: String = "DELETE FROM event_logs"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
