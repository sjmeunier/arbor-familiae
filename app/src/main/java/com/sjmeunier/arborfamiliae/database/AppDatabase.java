package com.sjmeunier.arborfamiliae.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import java.util.List;

@Database(entities = {
        Tree.class,
        Individual.class,
        IndividualNote.class,
        IndividualSource.class,
        Family.class,
        FamilySource.class,
        FamilyChild.class,
        FamilyNote.class,
        Source.class,
        Note.class,
        Place.class
    }, version = 9, exportSchema = false)
@TypeConverters({DateTypeConverter.class, GenderEnumConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract TreeDao treeDao();
    public abstract IndividualDao individualDao();
    public abstract IndividualSourceDao individualSourceDao();
    public abstract IndividualNoteDao individualNoteDao();
    public abstract FamilyDao familyDao();
    public abstract FamilyChildDao familyChildDao();
    public abstract FamilySourceDao familySourceDao();
    public abstract FamilyNoteDao familyNoteDao();
    public abstract NoteDao noteDao();
    public abstract SourceDao sourceDao();
    public abstract PlaceDao placeDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "arborfamiliae-database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
