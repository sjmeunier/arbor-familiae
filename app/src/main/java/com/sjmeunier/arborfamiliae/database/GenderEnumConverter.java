package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;


public class GenderEnumConverter {
    @TypeConverter
    public static GenderEnum toGenderEnum(int value) {
        return GenderEnum.values()[value];
    }

    @TypeConverter
    public static int toInt(GenderEnum value) {
        return value.ordinal();
    }
}
