package com.tungvuong.dangeralarm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.facebook.login.widget.ProfilePictureView.TAG;

public class receive extends IntentService {
    public receive() {
        super("receive");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        initChannels(this);
        FirebaseDatabase.getInstance().getReference()
                .child("danger-alram")
                .orderByChild("messageTime")
                .startAt("1561993850343") // last message time stamp goes here for first time it is zero
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String tinmoi = dataSnapshot.child("messageText").getValue(String.class);
                        Intent intent = new Intent(receive.this, chat.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(receive.this, 0, intent, 0);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(receive.this, "default");

                        notificationBuilder.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.emergency_icon)
                                .setContentTitle("Có tin mới!")
                                .setContentText(tinmoi)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                        .build();

                        NotificationManager notificationManager = (NotificationManager) receive.this.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, notificationBuilder.build());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }
}

