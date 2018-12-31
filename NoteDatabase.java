package com.example.ebpearls.architecturalcomponents;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase instance;

    public abstract NoteDao noteDao();

    public  static synchronized NoteDatabase getInstance(Context context)
    {
        if(instance == null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NoteDatabase.class, "note_database")
                    .fallbackToDestructiveMigration().addCallback(sRoomDatabaseCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback() {
                @Override
                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                    super.onOpen(db);
                    Completable.fromAction(() -> PopulateDb(instance))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                    Log.d("subscription", "subscribed");
                                }

                                @Override
                                public void onComplete() {
                                    Log.d("Completion", "completed");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d("Error", e.toString());
                                }
                            });
                }
            };


    /**
     * Populate the database in the background.
     */

    private static void PopulateDb(NoteDatabase db) {
        NoteDao mDao;
        Note note = new Note("Hello","World", 1);
        mDao = db.noteDao();
        mDao.deleteAllNotes();
        mDao.insert(note);
    }
}
