package de.vertedge.ssiwallet.data.SSI;

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

@Database(entities = {SSI_Message.class, SSI_Identity.class, SSI_VerifiableCredential.class, SSI_Signature.class, SSI_Authority.class}, version = 1)
public abstract class SSI_Database extends RoomDatabase {

    private static SSI_Database INSTANCE;

    public abstract SSI_Message.SSIM_Dao messageDao();
    public abstract SSI_Identity.SSI_Identity_Dao identityDao();
    public abstract SSI_VerifiableCredential.SSI_VC_Dao vcDao();
    public abstract SSI_Signature.SSI_Signature_Dao ssigDao();
    public abstract SSI_Authority.SSI_Authority_Dao ssiAuthorityDao();

    public synchronized static SSI_Database getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    private static SSI_Database buildDatabase(final Context context) {
        return Room.databaseBuilder(context,
                SSI_Database.class,
                "database-ssi").allowMainThreadQueries()
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                SSI_Database db = getInstance(context);
                                db.ssiAuthorityDao().insertAll(SSI_Authority.prepopulatedData());
                                long authority = db.ssiAuthorityDao().findByName(SSI_Authority.DEFAULT_AUTHORITY).get_uid();
                                long uid1 = db.identityDao().insert( SSI_Identity.prepopulatedData(authority)[0] );
                                long uid2 = db.identityDao().insert( SSI_Identity.prepopulatedData(authority)[1] );
                                long uid3 = db.identityDao().insert( SSI_Identity.prepopulatedData(authority)[2] );
                                db.identityDao().insert( SSI_Identity.prepopulatedData(authority)[3] );
                                long uid4 = db.identityDao().insert( SSI_Identity.prepopulatedData(authority)[4] );

                                db.vcDao().insertAll( SSI_VerifiableCredential.prepopulatedData(uid1, uid2, uid3, uid4) );
                                db.ssigDao().insertAll( SSI_Signature.prepopulatedData() );
                            }
                        });
                    }
                })
                .build();
    }

    public Long findVCbyClaim(Context context, String claimed){
        long result = -1;
        SSI_Database db = getInstance(context);
        List<SSI_VerifiableCredential> vcs = db.vcDao().getAll();
        for (SSI_VerifiableCredential vc : vcs){
            for (SSI_Claim claim : vc.get_claims()){
               if (claim.toString().equals(claimed)){
                   return vc.get_uid();
               }
            }
        }
        return result;
    }
}

