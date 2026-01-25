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
public final class PersonaStatisticsDao_Impl implements PersonaStatisticsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PersonaStatistics> __insertionAdapterOfPersonaStatistics;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldStatistics;

  public PersonaStatisticsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPersonaStatistics = new EntityInsertionAdapter<PersonaStatistics>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `persona_statistics` (`id`,`personaId`,`timestamp`,`openCount`,`totalTasks`,`completedTasks`,`score`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PersonaStatistics entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPersonaId());
        statement.bindLong(3, entity.getTimestamp());
        statement.bindLong(4, entity.getOpenCount());
        statement.bindLong(5, entity.getTotalTasks());
        statement.bindLong(6, entity.getCompletedTasks());
        statement.bindDouble(7, entity.getScore());
      }
    };
    this.__preparedStmtOfDeleteOldStatistics = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persona_statistics WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertStatistics(final PersonaStatistics statistics,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPersonaStatistics.insertAndReturnId(statistics);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldStatistics(final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldStatistics.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
          __preparedStmtOfDeleteOldStatistics.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getStatisticsForPersona(final long personaId, final long startTime,
      final Continuation<? super List<PersonaStatistics>> $completion) {
    final String _sql = "SELECT * FROM persona_statistics WHERE personaId = ? AND timestamp >= ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonaStatistics>>() {
      @Override
      @NonNull
      public List<PersonaStatistics> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "openCount");
          final int _cursorIndexOfTotalTasks = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTasks");
          final int _cursorIndexOfCompletedTasks = CursorUtil.getColumnIndexOrThrow(_cursor, "completedTasks");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final List<PersonaStatistics> _result = new ArrayList<PersonaStatistics>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaStatistics _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpOpenCount;
            _tmpOpenCount = _cursor.getInt(_cursorIndexOfOpenCount);
            final int _tmpTotalTasks;
            _tmpTotalTasks = _cursor.getInt(_cursorIndexOfTotalTasks);
            final int _tmpCompletedTasks;
            _tmpCompletedTasks = _cursor.getInt(_cursorIndexOfCompletedTasks);
            final double _tmpScore;
            _tmpScore = _cursor.getDouble(_cursorIndexOfScore);
            _item = new PersonaStatistics(_tmpId,_tmpPersonaId,_tmpTimestamp,_tmpOpenCount,_tmpTotalTasks,_tmpCompletedTasks,_tmpScore);
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
  public Object getStatisticsSince(final long startTime,
      final Continuation<? super List<PersonaStatistics>> $completion) {
    final String _sql = "SELECT * FROM persona_statistics WHERE timestamp >= ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonaStatistics>>() {
      @Override
      @NonNull
      public List<PersonaStatistics> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "openCount");
          final int _cursorIndexOfTotalTasks = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTasks");
          final int _cursorIndexOfCompletedTasks = CursorUtil.getColumnIndexOrThrow(_cursor, "completedTasks");
          final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
          final List<PersonaStatistics> _result = new ArrayList<PersonaStatistics>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaStatistics _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpOpenCount;
            _tmpOpenCount = _cursor.getInt(_cursorIndexOfOpenCount);
            final int _tmpTotalTasks;
            _tmpTotalTasks = _cursor.getInt(_cursorIndexOfTotalTasks);
            final int _tmpCompletedTasks;
            _tmpCompletedTasks = _cursor.getInt(_cursorIndexOfCompletedTasks);
            final double _tmpScore;
            _tmpScore = _cursor.getDouble(_cursorIndexOfScore);
            _item = new PersonaStatistics(_tmpId,_tmpPersonaId,_tmpTimestamp,_tmpOpenCount,_tmpTotalTasks,_tmpCompletedTasks,_tmpScore);
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
