package com.samsara.polymath.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PersonaOpenEventDao_Impl implements PersonaOpenEventDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PersonaOpenEvent> __insertionAdapterOfPersonaOpenEvent;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldEvents;

  public PersonaOpenEventDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPersonaOpenEvent = new EntityInsertionAdapter<PersonaOpenEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `persona_open_events` (`id`,`personaId`,`timestamp`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PersonaOpenEvent entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPersonaId());
        statement.bindLong(3, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteOldEvents = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persona_open_events WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PersonaOpenEvent event, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPersonaOpenEvent.insertAndReturnId(event);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldEvents(final long beforeMillis,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldEvents.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, beforeMillis);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldEvents.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getDailyOpenCountsByPersona(final long personaId, final long sinceMillis,
      final Continuation<? super List<DailyOpenCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT (timestamp / 86400000) * 86400000 AS dayMillis,\n"
            + "               COUNT(*) AS openCount\n"
            + "        FROM persona_open_events\n"
            + "        WHERE personaId = ? AND timestamp >= ?\n"
            + "        GROUP BY timestamp / 86400000\n"
            + "        ORDER BY dayMillis ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, sinceMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyOpenCount>>() {
      @Override
      @NonNull
      public List<DailyOpenCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDayMillis = 0;
          final int _cursorIndexOfOpenCount = 1;
          final List<DailyOpenCount> _result = new ArrayList<DailyOpenCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyOpenCount _item;
            final long _tmpDayMillis;
            _tmpDayMillis = _cursor.getLong(_cursorIndexOfDayMillis);
            final long _tmpOpenCount;
            _tmpOpenCount = _cursor.getLong(_cursorIndexOfOpenCount);
            _item = new DailyOpenCount(_tmpDayMillis,_tmpOpenCount);
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
  public Object getDailyOpenCountsGlobal(final long sinceMillis,
      final Continuation<? super List<DailyOpenCount>> $completion) {
    final String _sql = "\n"
            + "        SELECT (timestamp / 86400000) * 86400000 AS dayMillis,\n"
            + "               COUNT(*) AS openCount\n"
            + "        FROM persona_open_events\n"
            + "        WHERE timestamp >= ?\n"
            + "        GROUP BY timestamp / 86400000\n"
            + "        ORDER BY dayMillis ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DailyOpenCount>>() {
      @Override
      @NonNull
      public List<DailyOpenCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDayMillis = 0;
          final int _cursorIndexOfOpenCount = 1;
          final List<DailyOpenCount> _result = new ArrayList<DailyOpenCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyOpenCount _item;
            final long _tmpDayMillis;
            _tmpDayMillis = _cursor.getLong(_cursorIndexOfDayMillis);
            final long _tmpOpenCount;
            _tmpOpenCount = _cursor.getLong(_cursorIndexOfOpenCount);
            _item = new DailyOpenCount(_tmpDayMillis,_tmpOpenCount);
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
  public Object getAllEventsSince(final long sinceMillis,
      final Continuation<? super List<PersonaOpenEvent>> $completion) {
    final String _sql = "SELECT * FROM persona_open_events WHERE timestamp >= ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sinceMillis);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonaOpenEvent>>() {
      @Override
      @NonNull
      public List<PersonaOpenEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<PersonaOpenEvent> _result = new ArrayList<PersonaOpenEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaOpenEvent _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new PersonaOpenEvent(_tmpId,_tmpPersonaId,_tmpTimestamp);
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
