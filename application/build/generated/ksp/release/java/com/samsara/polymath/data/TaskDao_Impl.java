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
import java.lang.Integer;
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
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Task> __insertionAdapterOfTask;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<Task> __deletionAdapterOfTask;

  private final EntityDeletionOrUpdateAdapter<Task> __updateAdapterOfTask;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTaskOrder;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTaskOrderWithRank;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTaskCompletion;

  public TaskDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTask = new EntityInsertionAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `tasks` (`id`,`personaId`,`title`,`description`,`createdAt`,`completedAt`,`isCompleted`,`isRecurring`,`order`,`backgroundColor`,`previousOrder`,`rankStatus`,`recurringFrequency`,`recurringDays`,`endDate`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Task entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPersonaId());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDescription());
        statement.bindLong(5, entity.getCreatedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCompletedAt());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isRecurring() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getOrder());
        statement.bindString(10, entity.getBackgroundColor());
        statement.bindLong(11, entity.getPreviousOrder());
        final String _tmp_2 = __converters.fromRankStatus(entity.getRankStatus());
        statement.bindString(12, _tmp_2);
        if (entity.getRecurringFrequency() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRecurringFrequency());
        }
        if (entity.getRecurringDays() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRecurringDays());
        }
        if (entity.getEndDate() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getEndDate());
        }
      }
    };
    this.__deletionAdapterOfTask = new EntityDeletionOrUpdateAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `tasks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Task entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTask = new EntityDeletionOrUpdateAdapter<Task>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `tasks` SET `id` = ?,`personaId` = ?,`title` = ?,`description` = ?,`createdAt` = ?,`completedAt` = ?,`isCompleted` = ?,`isRecurring` = ?,`order` = ?,`backgroundColor` = ?,`previousOrder` = ?,`rankStatus` = ?,`recurringFrequency` = ?,`recurringDays` = ?,`endDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Task entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getPersonaId());
        statement.bindString(3, entity.getTitle());
        statement.bindString(4, entity.getDescription());
        statement.bindLong(5, entity.getCreatedAt());
        if (entity.getCompletedAt() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCompletedAt());
        }
        final int _tmp = entity.isCompleted() ? 1 : 0;
        statement.bindLong(7, _tmp);
        final int _tmp_1 = entity.isRecurring() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        statement.bindLong(9, entity.getOrder());
        statement.bindString(10, entity.getBackgroundColor());
        statement.bindLong(11, entity.getPreviousOrder());
        final String _tmp_2 = __converters.fromRankStatus(entity.getRankStatus());
        statement.bindString(12, _tmp_2);
        if (entity.getRecurringFrequency() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getRecurringFrequency());
        }
        if (entity.getRecurringDays() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getRecurringDays());
        }
        if (entity.getEndDate() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getEndDate());
        }
        statement.bindLong(16, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateTaskOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET `order` = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTaskOrderWithRank = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET `order` = ?, previousOrder = ?, rankStatus = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTaskCompletion = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tasks SET isCompleted = ?, completedAt = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTask(final Task task, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTask.insertAndReturnId(task);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTask(final Task task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTask.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTask(final Task task, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTask.handle(task);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTaskOrder(final long id, final int order,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTaskOrder.acquire();
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
          __preparedStmtOfUpdateTaskOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTaskOrderWithRank(final long id, final int order, final int previousOrder,
      final String rankStatus, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTaskOrderWithRank.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, order);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, previousOrder);
        _argIndex = 3;
        _stmt.bindString(_argIndex, rankStatus);
        _argIndex = 4;
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
          __preparedStmtOfUpdateTaskOrderWithRank.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTaskCompletion(final long id, final boolean isCompleted,
      final Long completedAt, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTaskCompletion.acquire();
        int _argIndex = 1;
        final int _tmp = isCompleted ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        if (completedAt == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, completedAt);
        }
        _argIndex = 3;
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
          __preparedStmtOfUpdateTaskCompletion.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Task>> getTasksByPersona(final long personaId) {
    final String _sql = "SELECT * FROM tasks WHERE personaId = ? ORDER BY `order` ASC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Flow<List<Task>> getAllTasks() {
    final String _sql = "SELECT * FROM tasks ORDER BY personaId ASC, `order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Object getTaskById(final long id, final Continuation<? super Task> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Task>() {
      @Override
      @Nullable
      public Task call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final Task _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _result = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Object getCompletedTaskCount(final long personaId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM tasks WHERE personaId = ? AND isCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getTasksByPersonaList(final long personaId,
      final Continuation<? super List<Task>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE personaId = ? ORDER BY `order` ASC, createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Flow<List<Task>> getAllOpenRecurringTasks() {
    final String _sql = "SELECT * FROM tasks WHERE isRecurring = 1 AND isCompleted = 0 ORDER BY personaId ASC, `order` ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Flow<List<Task>> getAllTasksWithEndDate() {
    final String _sql = "SELECT * FROM tasks WHERE endDate IS NOT NULL AND isCompleted = 0 ORDER BY endDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tasks"}, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
  public Object getCompletedRecurringInstances(final long personaId, final String title,
      final Continuation<? super List<Task>> $completion) {
    final String _sql = "SELECT * FROM tasks WHERE personaId = ? AND title = ? AND isRecurring = 1 AND isCompleted = 1 ORDER BY completedAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, personaId);
    _argIndex = 2;
    _statement.bindString(_argIndex, title);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Task>>() {
      @Override
      @NonNull
      public List<Task> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPersonaId = CursorUtil.getColumnIndexOrThrow(_cursor, "personaId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfCompletedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "completedAt");
          final int _cursorIndexOfIsCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "isCompleted");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "order");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfPreviousOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "previousOrder");
          final int _cursorIndexOfRankStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "rankStatus");
          final int _cursorIndexOfRecurringFrequency = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringFrequency");
          final int _cursorIndexOfRecurringDays = CursorUtil.getColumnIndexOrThrow(_cursor, "recurringDays");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final List<Task> _result = new ArrayList<Task>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Task _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpPersonaId;
            _tmpPersonaId = _cursor.getLong(_cursorIndexOfPersonaId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpCompletedAt;
            if (_cursor.isNull(_cursorIndexOfCompletedAt)) {
              _tmpCompletedAt = null;
            } else {
              _tmpCompletedAt = _cursor.getLong(_cursorIndexOfCompletedAt);
            }
            final boolean _tmpIsCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCompleted);
            _tmpIsCompleted = _tmp != 0;
            final boolean _tmpIsRecurring;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_1 != 0;
            final int _tmpOrder;
            _tmpOrder = _cursor.getInt(_cursorIndexOfOrder);
            final String _tmpBackgroundColor;
            _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            final int _tmpPreviousOrder;
            _tmpPreviousOrder = _cursor.getInt(_cursorIndexOfPreviousOrder);
            final RankStatus _tmpRankStatus;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfRankStatus);
            _tmpRankStatus = __converters.toRankStatus(_tmp_2);
            final String _tmpRecurringFrequency;
            if (_cursor.isNull(_cursorIndexOfRecurringFrequency)) {
              _tmpRecurringFrequency = null;
            } else {
              _tmpRecurringFrequency = _cursor.getString(_cursorIndexOfRecurringFrequency);
            }
            final String _tmpRecurringDays;
            if (_cursor.isNull(_cursorIndexOfRecurringDays)) {
              _tmpRecurringDays = null;
            } else {
              _tmpRecurringDays = _cursor.getString(_cursorIndexOfRecurringDays);
            }
            final Long _tmpEndDate;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmpEndDate = null;
            } else {
              _tmpEndDate = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _item = new Task(_tmpId,_tmpPersonaId,_tmpTitle,_tmpDescription,_tmpCreatedAt,_tmpCompletedAt,_tmpIsCompleted,_tmpIsRecurring,_tmpOrder,_tmpBackgroundColor,_tmpPreviousOrder,_tmpRankStatus,_tmpRecurringFrequency,_tmpRecurringDays,_tmpEndDate);
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
