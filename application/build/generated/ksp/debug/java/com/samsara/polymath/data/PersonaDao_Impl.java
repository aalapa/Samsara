package com.samsara.polymath.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
        return "INSERT OR ABORT INTO `personas` (`id`,`name`,`createdAt`,`order`,`openCount`,`backgroundColor`,`textColor`,`previousOpenCount`,`rankStatus`,`lastOpenedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
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
        statement.bindLong(10, entity.getLastOpenedAt());
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
        return "UPDATE OR ABORT `personas` SET `id` = ?,`name` = ?,`createdAt` = ?,`order` = ?,`openCount` = ?,`backgroundColor` = ?,`textColor` = ?,`previousOpenCount` = ?,`rankStatus` = ?,`lastOpenedAt` = ? WHERE `id` = ?";
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
        statement.bindLong(10, entity.getLastOpenedAt());
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfIncrementOpenCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE personas SET openCount = openCount + 1, lastOpenedAt = ? WHERE id = ?";
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
  public Object incrementOpenCount(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementOpenCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
    final String _sql = "SELECT * FROM personas ORDER BY createdAt ASC";
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
          final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
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
            final long _tmpLastOpenedAt;
            _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
            _item = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
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
          final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
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
            final long _tmpLastOpenedAt;
            _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
            _result = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
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
  public Object getAllPersonasList(final Continuation<? super List<Persona>> $completion) {
    final String _sql = "SELECT * FROM personas ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Persona>>() {
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
          final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
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
            final long _tmpLastOpenedAt;
            _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
            _item = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
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
  public Flow<List<PersonaWithTags>> getAllPersonasWithTags() {
    final String _sql = "SELECT * FROM personas ORDER BY `order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"persona_tags", "tags",
        "personas"}, new Callable<List<PersonaWithTags>>() {
      @Override
      @NonNull
      public List<PersonaWithTags> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
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
            final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
            final LongSparseArray<ArrayList<Tag>> _collectionTags = new LongSparseArray<ArrayList<Tag>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<Tag>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomSamsaraPolymathDataTag(_collectionTags);
            final List<PersonaWithTags> _result = new ArrayList<PersonaWithTags>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final PersonaWithTags _item;
              final Persona _tmpPersona;
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
              final long _tmpLastOpenedAt;
              _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
              _tmpPersona = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
              final ArrayList<Tag> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _item = new PersonaWithTags(_tmpPersona,_tmpTagsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getPersonaWithTags(final long personaId,
      final Continuation<? super PersonaWithTags> $completion) {
    final String _sql = "SELECT * FROM personas WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, true, _cancellationSignal, new Callable<PersonaWithTags>() {
      @Override
      @Nullable
      public PersonaWithTags call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
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
            final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
            final LongSparseArray<ArrayList<Tag>> _collectionTags = new LongSparseArray<ArrayList<Tag>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionTags.containsKey(_tmpKey)) {
                _collectionTags.put(_tmpKey, new ArrayList<Tag>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshiptagsAscomSamsaraPolymathDataTag(_collectionTags);
            final PersonaWithTags _result;
            if (_cursor.moveToFirst()) {
              final Persona _tmpPersona;
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
              final long _tmpLastOpenedAt;
              _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
              _tmpPersona = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
              final ArrayList<Tag> _tmpTagsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpTagsCollection = _collectionTags.get(_tmpKey_1);
              _result = new PersonaWithTags(_tmpPersona,_tmpTagsCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
            _statement.release();
          }
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Persona>> getPersonasByTags(final List<Long> tagIds) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT DISTINCT personas.* FROM personas INNER JOIN persona_tags ON personas.id = persona_tags.personaId WHERE persona_tags.tagId IN (");
    final int _inputSize = tagIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") ORDER BY personas.`order` ASC");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : tagIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"personas",
        "persona_tags"}, new Callable<List<Persona>>() {
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
          final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
          final List<Persona> _result = new ArrayList<Persona>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Persona _item_1;
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
            final long _tmpLastOpenedAt;
            _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
            _item_1 = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
            _result.add(_item_1);
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
  public Flow<List<Persona>> getUntaggedPersonas() {
    final String _sql = "SELECT personas.* FROM personas LEFT JOIN persona_tags ON personas.id = persona_tags.personaId WHERE persona_tags.personaId IS NULL ORDER BY personas.`order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"personas",
        "persona_tags"}, new Callable<List<Persona>>() {
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
          final int _cursorIndexOfLastOpenedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastOpenedAt");
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
            final long _tmpLastOpenedAt;
            _tmpLastOpenedAt = _cursor.getLong(_cursorIndexOfLastOpenedAt);
            _item = new Persona(_tmpId,_tmpName,_tmpCreatedAt,_tmpOrder,_tmpOpenCount,_tmpBackgroundColor,_tmpTextColor,_tmpPreviousOpenCount,_tmpRankStatus,_tmpLastOpenedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshiptagsAscomSamsaraPolymathDataTag(
      @NonNull final LongSparseArray<ArrayList<Tag>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshiptagsAscomSamsaraPolymathDataTag(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `tags`.`id` AS `id`,`tags`.`name` AS `name`,`tags`.`color` AS `color`,`tags`.`created_at` AS `created_at`,`tags`.`order` AS `order`,_junction.`personaId` FROM `persona_tags` AS _junction INNER JOIN `tags` ON (_junction.`tagId` = `tags`.`id`) WHERE _junction.`personaId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      // _junction.personaId;
      final int _itemKeyIndex = 5;
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfName = 1;
      final int _cursorIndexOfColor = 2;
      final int _cursorIndexOfCreatedAt = 3;
      final int _cursorIndexOfOrder = 4;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<Tag> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final Tag _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final String _tmpName;
          _tmpName = _cursor.getString(_cursorIndexOfName);
          final String _tmpColor;
          if (_cursor.isNull(_cursorIndexOfColor)) {
            _tmpColor = null;
          } else {
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
          }
          final long _tmpCreatedAt;
          _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
          final int _tmpOrder;
          _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
          _item_1 = new Tag(_tmpId,_tmpName,_tmpColor,_tmpCreatedAt,_tmpOrder);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
