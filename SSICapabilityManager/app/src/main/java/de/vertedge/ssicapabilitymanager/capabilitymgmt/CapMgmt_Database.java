package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(entities = {User.class, Capability.class, Joblisting.class, Organisation.class, Message.class}, version = 1)
public abstract class CapMgmt_Database extends RoomDatabase {

    private static CapMgmt_Database INSTANCE;

    public abstract Capability.Cap_Dao capDao();

    public abstract Joblisting.Jobs_Dao jobsDao();

    public abstract Organisation.Org_Dao orgsDao();

    public abstract Message.SSIM_Dao messagesDao();

    public abstract User.UserDao usersDao();

    public synchronized static CapMgmt_Database getInstance(Context context) {
        if (INSTANCE == null) INSTANCE = buildDatabase(context);
        return INSTANCE;
    }

    private static CapMgmt_Database buildDatabase(final Context context) {
        return Room.databaseBuilder(context,
                CapMgmt_Database.class,
                "database-capmgmt").allowMainThreadQueries()
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                CapMgmt_Database db = getInstance(context);
                                long cap1 = db.capDao().insert(Capability.prepopulatedData()[0]);
                                long cap2 = db.capDao().insert(Capability.prepopulatedData()[1]);
                                long cap3 = db.capDao().insert(Capability.prepopulatedData()[2]);
                                long cap4 = db.capDao().insert(Capability.prepopulatedData()[3]);
                                db.capDao().insert(Capability.prepopulatedData()[4]);
                                db.capDao().insert(Capability.prepopulatedData()[5]);

                                db.jobsDao().insertAll(Joblisting.prepopulatedData(cap1, cap2, cap3, cap4));
                                db.orgsDao().insertAll(Organisation.prepopulatedData());
                                db.messagesDao().insertAll(Message.prepopulatedData(cap1, cap2, cap3, context));
                                db.usersDao().insertAll(User.prepopulatedData(0L, 1L));

                                for (User user : db.usersDao().getAll()) {
                                    Log.i(this.toString(), "created user: " + user.get_ssidid() + " with id " + user.get_uid());
                                }
                            }
                        });
                    }
                })
                .build();
    }
}