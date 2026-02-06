package com.samsara.polymath.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TimeEntryDao_Impl implements TimeEntryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TimeEntry> __insertionAdapterOfTimeEntry;

  private final EntityDeletionOrUpdateAdapter<TimeEntry> __updateAdapterOfTimeEntry;

  public TimeEntryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTimeEntry = new EntityInsertionAdapter<TimeEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `time_entries` (`id`,`taskId`,`startTime`,`endTime`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTime());
        }
      }
    };
    this.__updateAdapterOfTimeEntry = new EntityDeletionOrUpdateAdapter<TimeEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `time_entries` SET `id` = ?,`taskId` = ?,`startTime` = ?,`endTime` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TimeEntry entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTaskId());
        statement.bindLong(3, entity.getStartTime());
        if (entity.getEndTime() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTime());
        }
        statement.bindLong(5, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final TimeEntry entry, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTimeEntry.insertAndReturnId(entry);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final TimeEntry entry, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTimeEntry.handle(entry);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRunningEntry(final long taskId,
      final Continuation<? super TimeEntry> $completion) {
    final String _sql = "SELECT * FROM time_entries WHERE taskId = ? AND endTime IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TimeEntry>() {
      @Override
      @Nullable
      public TimeEntry call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final TimeEntry _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            _result = new TimeEntry(_tmpId,_tmpTaskId,_tmpStartTime,_tmpEndTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRunningEntryAnyTask(final Continuation<? super TimeEntry> $completion) {
    final String _sql = "SELECT * FROM time_entries WHERE endTime IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TimeEntry>() {
      @Override
      @Nullable
      public TimeEntry call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final TimeEntry _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            _result = new TimeEntry(_tmpId,_tmpTaskId,_tmpStartTime,_tmpEndTime);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TimeEntry>> getEntriesByTask(final long taskId) {
    final String _sql = "SELECT * FROM time_entries WHERE taskId = ? ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_entries"}, new Callable<List<TimeEntry>>() {
      @Override
      @NonNull
      public List<TimeEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<TimeEntry> _result = new ArrayList<TimeEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            _item = new TimeEntry(_tmpId,_tmpTaskId,_tmpStartTime,_tmpEndTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTotalTimeByTask(final long taskId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COALESCE(SUM(endTime - startTime), 0) FROM time_entries WHERE taskId = ? AND endTime IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, taskId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTaskTimeSumsByPersona(final long personaId,
      final Continuation<? super List<TaskTimeSum>> $completion) {
    final String _sql = "\n"
            + "        SELECT t.id AS taskId, t.title AS taskTitle, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime\n"
            + "        FROM tasks t\n"
            + "        LEFT JOIN time_entries te ON t.id = te.taskId AND te.endTime IS NOT NULL\n"
            + "        WHERE t.personaId = ?\n"
            + "        GROUP BY t.id\n"
            + "        HAVING totalTime > 0\n"
            + "        ORDER BY totalTime DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TaskTimeSum>>() {
      @Override
      @NonNull
      public List<TaskTimeSum> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTaskId = 0;
          final int _cursorIndexOfTaskTitle = 1;
          final int _cursorIndexOfTotalTime = 2;
          final List<TaskTimeSum> _result = new ArrayList<TaskTimeSum>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskTimeSum _item;
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final String _tmpTaskTitle;
            _tmpTaskTitle = _cursor.getString(_cursorIndexOfTaskTitle);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new TaskTimeSum(_tmpTaskId,_tmpTaskTitle,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalTimeByAllPersonas(
      final Continuation<? super List<PersonaTimeSum>> $completion) {
    final String _sql = "\n"
            + "        SELECT t.personaId AS personaId, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime\n"
            + "        FROM time_entries te\n"
            + "        INNER JOIN tasks t ON te.taskId = t.id\n"
            + "        WHERE te.endTime IS NOT NULL\n"
            + "        GROUP BY t.personaId\n"
            + "        ORDER BY totalTime DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonaTimeSum>>() {
      @Override
      @NonNull
      public List<PersonaTimeSum> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonaId = 0;
          final int _cursorIndexOfTotalTime = 1;
          final List<PersonaTimeSum> _result = new ArrayList<PersonaTimeSum>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaTimeSum _item;
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new PersonaTimeSum(_tmpPersonaId,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllCompletedEntries(final Continuation<? super List<TimeEntry>> $completion) {
    final String _sql = "SELECT * FROM time_entries WHERE endTime IS NOT NULL ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TimeEntry>>() {
      @Override
      @NonNull
      public List<TimeEntry> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTaskId = CursorUtil.getColumnIndexOrThrow(_cursor, "taskId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final List<TimeEntry> _result = new ArrayList<TimeEntry>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TimeEntry _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final Long _tmpEndTime;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmpEndTime = null;
            } else {
              _tmpEndTime = _cursor.getLong(_cursorIndexOfEndTime);
            }
            _item = new TimeEntry(_tmpId,_tmpTaskId,_tmpStartTime,_tmpEndTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Long> getRunningTaskIdFlow() {
    final String _sql = "SELECT taskId FROM time_entries WHERE endTime IS NULL LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_entries"}, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getLong(0);
            }
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TaskIdTime>> getTotalTimesByPersonaFlow(final long personaId) {
    final String _sql = "\n"
            + "        SELECT te.taskId AS taskId, COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime\n"
            + "        FROM time_entries te\n"
            + "        INNER JOIN tasks t ON te.taskId = t.id\n"
            + "        WHERE t.personaId = ? AND te.endTime IS NOT NULL\n"
            + "        GROUP BY te.taskId\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"time_entries",
        "tasks"}, new Callable<List<TaskIdTime>>() {
      @Override
      @NonNull
      public List<TaskIdTime> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTaskId = 0;
          final int _cursorIndexOfTotalTime = 1;
          final List<TaskIdTime> _result = new ArrayList<TaskIdTime>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TaskIdTime _item;
            final long _tmpTaskId;
            _tmpTaskId = _cursor.getLong(_cursorIndexOfTaskId);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new TaskIdTime(_tmpTaskId,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getDailyTimeSums(final long sinceMillis,
      final Continuation<? super List<DailyTimeSum>> $completion) {
    final String _sql = "\n"
            + "        SELECT (te.startTime / 86400000) * 86400000 AS dayMillis,\n"
            + "               COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime\n"
            + "        FROM time_entries te\n"
            + "        WHERE te.endTime IS NOT NULL AND te.startTime >= ?\n"
            + "        GROUP BY te.startTime / 86400000\n"
            + "        ORDER BY dayMillis ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyTimeSum>>() {
      @Override
      @NonNull
      public List<DailyTimeSum> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDayMillis = 0;
          final int _cursorIndexOfTotalTime = 1;
          final List<DailyTimeSum> _result = new ArrayList<DailyTimeSum>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyTimeSum _item;
            final long _tmpDayMillis;
            _tmpDayMillis = _cursor.getLong(_cursorIndexOfDayMillis);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new DailyTimeSum(_tmpDayMillis,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTimeBreakdownForDay(final long dayMillis,
      final Continuation<? super List<DailyPersonaTimeSum>> $completion) {
    final String _sql = "\n"
            + "        SELECT t.personaId AS personaId, p.name AS personaName,\n"
            + "               COALESCE(SUM(te.endTime - te.startTime), 0) AS totalTime\n"
            + "        FROM time_entries te\n"
            + "        INNER JOIN tasks t ON te.taskId = t.id\n"
            + "        INNER JOIN personas p ON t.personaId = p.id\n"
            + "        WHERE te.endTime IS NOT NULL\n"
            + "          AND (te.startTime / 86400000) = ? / 86400000\n"
            + "        GROUP BY t.personaId\n"
            + "        ORDER BY totalTime DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyPersonaTimeSum>>() {
      @Override
      @NonNull
      public List<DailyPersonaTimeSum> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonaId = 0;
          final int _cursorIndexOfPersonaName = 1;
          final int _cursorIndexOfTotalTime = 2;
          final List<DailyPersonaTimeSum> _result = new ArrayList<DailyPersonaTimeSum>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyPersonaTimeSum _item;
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpPersonaName;
            _tmpPersonaName = _cursor.getString(_cursorIndexOfPersonaName);
            final long _tmpTotalTime;
            _tmpTotalTime = _cursor.getLong(_cursorIndexOfTotalTime);
            _item = new DailyPersonaTimeSum(_tmpPersonaId,_tmpPersonaName,_tmpTotalTime);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
