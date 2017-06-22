package com.egoriku.catsrunning.utils;

import android.content.Context;
import android.widget.Toast;

import com.egoriku.catsrunning.activities.AddUserInfoActivity;
import com.egoriku.catsrunning.data.commons.TracksModel;
import com.egoriku.catsrunning.models.Firebase.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.egoriku.catsrunning.models.Constants.FirebaseFields.TRACKS;
import static com.egoriku.catsrunning.models.Constants.FirebaseFields.USER_INFO;

public class FirebaseUtils {

    private FirebaseUtils() {
    }

    private static FirebaseDatabase firebaseDatabase;
    private static FirebaseUser user;

    public static DatabaseReference getDatabaseReference() {
        if (firebaseDatabase == null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseDatabase.setPersistenceEnabled(true);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseDatabase.getReference();
    }

    public static FirebaseUser getUser() {
        return user;
    }

    public static void updateTrackFavorire(final TracksModel tracksModel, final Context context) {
        if (user != null && tracksModel.getTrackToken() != null) {
            getDatabaseReference()
                    .child(TRACKS)
                    .child(user.getUid())
                    .child(tracksModel.getTrackToken())
                    .setValue(tracksModel, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public static void saveUserInfo(UserInfo userInfo, final Context context) {
        if (user != null) {
            getDatabaseReference()
                    .child(USER_INFO)
                    .child(user.getUid())
                    .setValue(userInfo, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public static void removeTrack(TracksModel tracksModel, final Context context) {
        if (tracksModel.getTrackToken() != null && user != null) {
            getDatabaseReference()
                    .child(TRACKS)
                    .child(user.getUid())
                    .child(tracksModel.getTrackToken())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dataSnapshot.getRef().setValue(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void updateUserInfo(final Context context) {
        if (user != null) {
            getDatabaseReference()
                    .child(USER_INFO)
                    .child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                AddUserInfoActivity.start(context);
                            } else {
                                UserInfoPreferences userInfoPreferences = new UserInfoPreferences(context);
                                UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);

                                int growth = userInfo.getGrowth();
                                int weight = userInfo.getWeight();
                                int age = userInfo.getAge();

                                /*if (userInfoPreferences.getGrowth() != growth || userInfoPreferences.getWeight() != weight) {
                                    userInfoPreferences.writeUserData(growth, weight);
                                }*/
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }
}
