package com.samsara.polymath.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
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
public final class PersonaTagDao_Impl implements PersonaTagDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PersonaTag> __insertionAdapterOfPersonaTag;

  private final EntityDeletionOrUpdateAdapter<PersonaTag> __deletionAdapterOfPersonaTag;

  private final SharedSQLiteStatement __preparedStmtOfRemoveTagFromPersona;

  private final SharedSQLiteStatement __preparedStmtOfRemoveAllTagsFromPersona;

  private final SharedSQLiteStatement __preparedStmtOfRemoveTagFromAllPersonas;

  public PersonaTagDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPersonaTag = new EntityInsertionAdapter<PersonaTag>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `persona_tags` (`personaId`,`tagId`,`assigned_at`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PersonaTag entity) {
        statement.bindLong(1, entity.getPersonaId());
        statement.bindLong(2, entity.getTagId());
        statement.bindLong(3, entity.getAssignedAt());
      }
    };
    this.__deletionAdapterOfPersonaTag = new EntityDeletionOrUpdateAdapter<PersonaTag>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `persona_tags` WHERE `personaId` = ? AND `tagId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PersonaTag entity) {
        statement.bindLong(1, entity.getPersonaId());
        statement.bindLong(2, entity.getTagId());
      }
    };
    this.__preparedStmtOfRemoveTagFromPersona = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persona_tags WHERE personaId = ? AND tagId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveAllTagsFromPersona = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persona_tags WHERE personaId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveTagFromAllPersonas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM persona_tags WHERE tagId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPersonaTag(final PersonaTag personaTag,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPersonaTag.insert(personaTag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePersonaTag(final PersonaTag personaTag,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfPersonaTag.handle(personaTag);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object removeTagFromPersona(final long personaId, final long tagId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveTagFromPersona.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, personaId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, tagId);
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
          __preparedStmtOfRemoveTagFromPersona.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeAllTagsFromPersona(final long personaId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveAllTagsFromPersona.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, personaId);
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
          __preparedStmtOfRemoveAllTagsFromPersona.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object removeTagFromAllPersonas(final long tagId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveTagFromAllPersonas.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, tagId);
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
          __preparedStmtOfRemoveTagFromAllPersonas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object isTagAssignedToPersona(final long personaId, final long tagId,
      final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM persona_tags WHERE personaId = ? AND tagId = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, tagId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp != 0;
          } else {
            _result = false;
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
  public Object getAllSync(final Continuation<? super List<PersonaTag>> $completion) {
    final String _sql = "SELECT * FROM persona_tags";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PersonaTag>>() {
      @Override
      @NonNull
      public List<PersonaTag> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "tagId");
          final int _cursorIndexOfAssignedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "assigned_at");
          final List<PersonaTag> _result = new ArrayList<PersonaTag>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaTag _item;
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTagId;
            _tmpTagId = _cursor.getLong(_cursorIndexOfTagId);
            final long _tmpAssignedAt;
            _tmpAssignedAt = _cursor.getLong(_cursorIndexOfAssignedAt);
            _item = new PersonaTag(_tmpPersonaId,_tmpTagId,_tmpAssignedAt);
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
  public Flow<List<PersonaTag>> getAllPersonaTags() {
    final String _sql = "SELECT * FROM persona_tags";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"persona_tags"}, new Callable<List<PersonaTag>>() {
      @Override
      @NonNull
      public List<PersonaTag> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTagId = CursorUtil.getColumnIndexOrThrow(_cursor, "tagId");
          final int _cursorIndexOfAssignedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "assigned_at");
          final List<PersonaTag> _result = new ArrayList<PersonaTag>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PersonaTag _item;
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final long _tmpTagId;
            _tmpTagId = _cursor.getLong(_cursorIndexOfTagId);
            final long _tmpAssignedAt;
            _tmpAssignedAt = _cursor.getLong(_cursorIndexOfAssignedAt);
            _item = new PersonaTag(_tmpPersonaId,_tmpTagId,_tmpAssignedAt);
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
}
