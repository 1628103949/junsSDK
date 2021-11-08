package com.juns.sdk.framework.xutils.db.converter;

import android.database.Cursor;

import com.juns.sdk.framework.xutils.db.sqlite.ColumnDbType;

public interface ColumnConverter<T> {

    T getFieldValue(final Cursor cursor, int index);

    Object fieldValue2DbValue(T fieldValue);

    ColumnDbType getColumnDbType();
}
