package de.vertedge.ssicapabilitymanager.capabilitymgmt;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * android room database
 */
@Database(entities = {User.class, Capability.class, Joblisting.class, Organisation.class, Message.class}, version = 1)
public abstract class CapMgmt_Database extends RoomDatabase {

    private static CapMgmt_Database INSTANCE;

    public abstract Capability.Cap_Dao capDao();

    public abstract Joblisting.Jobs_Dao jobsDao();

    public abstract Organisation.Org_Dao orgsDao();

    public abstract Message.SSIM_Dao messagesDao();

    public abstract User.UserDao usersDao();

    /**
     * singleton instance
     *
     * @param context   application context to get the DB instance
     * @return  DB singleton
     */
    public synchronized static CapMgmt_Database getInstance(Context context) {
        if (INSTANCE == null) INSTANCE = buildDatabase(context);
        return INSTANCE;
    }

    /**
     * internal database builder
     *
     * @param context   application context to build the DB
     * @return  DB singleton
     */
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
                                db.messagesDao().insertAll(Message.prepopulatedData(cap1, context));
                                db.usersDao().insertAll(User.prepopulatedData(0L, 1L));

                                for (User user : db.usersDao().getAll()) {
                                    Log.d(this.toString(), "created user: " + user.get_ssidid() + " with id " + user.get_uid());
                                }
                            }
                        });
                    }
                })
                .build();
    }

    /**
     * fetch all messages from DB and filter out any that aren't for our user
     *
     * @param user  to_user DID adress
     * @return  list of messages that are meant for the DID user
     */
    public static List<Message> getUsersMessages(Context context, String user) {
        List<Message> _messages = getInstance(context).messagesDao().getAll();
        List<Message> result = new ArrayList<>();
        for (Message _message : _messages) {
            Log.d("DB, filtering messages", "user is '" + user + "', comparing to '" + _message.get_to_user() + "'");
            if (user.contains(_message.get_to_user())) {
                Log.d("DB found message", "user " + user + " has message " + _message.get_uid());
                result.add(_message);
            }
        }
        return result;
    }

    public static boolean hasMessages(Context context, String user) {
        List<Message> messages = CapMgmt_Database.getUsersMessages(context, user);
        return messages.size() > 0;
    }
}