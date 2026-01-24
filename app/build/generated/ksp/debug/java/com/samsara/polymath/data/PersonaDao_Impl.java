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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PersonaDao_Impl implements PersonaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Persona> __insertionAdapterOfPersona;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Persona> __deletionAdapterOfPersona;

  private final EntityDeletionOrUpdateAdapter<Persona> __updateAdapterOfPersona;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePersonaOrder;

  private final SharedSQLiteStatement __preparedStmtOfIncrementOpenCount;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePersonaName;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRankStatus;

  private final SharedSQLiteStatement __preparedStmtOfSavePreviousOpenCount;

  private final SharedSQLiteStatement __preparedStmtOfSaveAllPreviousOpenCounts;

  public PersonaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPersona = new EntityInsertionAdapter<Persona>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `personas` (`id`,`name`,`createdAt`,`order`,`openCount`,`backgroundColor`,`textColor`,`previousOpenCount`,`rankStatus`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Persona entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getOrder());
        statement.bindLong(5, entity.getOpenCount());
        statement.bindString(6, entity.getBackgroundColor());
        statement.bindString(7, entity.getTextColor());
        statement.bindLong(8, entity.getPreviousOpenCount());
        final String _tmp = __converters.fromRankStatus(entity.getRankStatus());
        statement.bindString(9, _tmp);
      }
    };
    this.__deletionAdapterOfPersona = new EntityDeletionOrUpdateAdapter<Persona>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `personas` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Persona entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfPersona = new EntityDeletionOrUpdateAdapter<Persona>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `personas` SET `id` = ?,`name` = ?,`createdAt` = ?,`order` = ?,`openCount` = ?,`backgroundColor` = ?,`textColor` = ?,`previousOpenCount` = ?,`rankStatus` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Persona entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getCreatedAt());
        statement.bindLong(4, entity.getOrder());
        statement.bindLong(5, entity.getOpenCount());
        statement.bindString(6, entity.getBackgroundColor());
        statement.bindString(7, entity.getTextColor());
        statement.bindLong(8, entity.getPreviousOpenCount());
        final String _tmp = __converters.fromRankStatus(entity.getRankStatus());
        statement.bindString(9, _tmp);
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfUpdatePersonaOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET `order` = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementOpenCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET openCount = openCount + 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePersonaName = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET name = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateRankStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET rankStatus = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSavePreviousOpenCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET previousOpenCount = openCount WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfSaveAllPreviousOpenCounts = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET previousOpenCount = openCount";
        return _query;
      }
    };
  }

  @Override
  public Object insertPersona(final Persona persona, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPersona.insertAndReturnId(persona);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePersona(final Persona persona, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPersona.handle(persona);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePersona(final Persona persona, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPersona.handle(persona);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePersonaOrder(final long id, final int order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePersonaOrder.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, order);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdatePersonaOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementOpenCount(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementOpenCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfIncrementOpenCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePersonaName(final long id, final String name,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePersonaName.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, name);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdatePersonaName.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRankStatus(final long id, final RankStatus rankStatus,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRankStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __converters.fromRankStatus(rankStatus);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfUpdateRankStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object savePreviousOpenCount(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSavePreviousOpenCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfSavePreviousOpenCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object saveAllPreviousOpenCounts(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSaveAllPreviousOpenCounts.acquire();
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
          __preparedStmtOfSaveAllPreviousOpenCounts.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Persona>> getAllPersonas() {
    final String _sql = "SELECT * FROM personas ORDER BY CASE WHEN `order` = 0 THEN 999999 ELSE `order` END ASC, openCount DESC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"personas"}, new Callable<List<Persona>>() {
      @Override
      @NonNull
      public List<Persona> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "openCount");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfPreviousOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOpenCount");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final List<Persona> _result = new ArrayList<Persona>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Persona _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final int _tmpOpenCount;
            _tmpOpenCount = _cursor.getInt(_cursorIndexOfOpenCount);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final String _tmpTextColor;
            _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            final int _tmpPreviousOpenCount;
            _tmpPreviousOpenCount = _cursor.getInt(_cursorIndexOfPreviousOpenCount);
            final RankStatus _tmpRankStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp);
            _item = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus);
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
  public Object getPersonaById(final long id, final Continuation<? super Persona> $completion) {
    final String _sql = "SELECT * FROM personas WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Persona>() {
      @Override
      @Nullable
      public Persona call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "openCount");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfPreviousOpenCount = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOpenCount");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final Persona _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final int _tmpOpenCount;
            _tmpOpenCount = _cursor.getInt(_cursorIndexOfOpenCount);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final String _tmpTextColor;
            _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            final int _tmpPreviousOpenCount;
            _tmpPreviousOpenCount = _cursor.getInt(_cursorIndexOfPreviousOpenCount);
            final RankStatus _tmpRankStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp);
            _result = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
