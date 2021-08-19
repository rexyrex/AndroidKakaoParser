package com.rexyrex.kakaoparser.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.rexyrex.kakaoparser.Activities.SplashActivity;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import java.io.File;

public class DeleteService extends Service {

    public static final String REX_CHANNEL_ID = "KAKAO_PARSER_CHANNEL_ID";
    public static final int REX_CHANNEL_NOTI_ID = 777;
    private NotificationManager notificationManager;
    ChatData cd;
    int filesDeleted;
    int totalFileCount;

    long totalSize;
    long deletedSize;

    public DeleteService() {

    }

    @Override
    public void onCreate(){
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        cd = ChatData.getInstance(this);

        filesDeleted = 0;
        deletedSize = 0;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getSizeRecursive(cd.getChatFile());
        Notification notification = getNotification("Rex Service", "Running");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(REX_CHANNEL_NOTI_ID,notification);
        }

        deleteFile(cd.getChatFile());

        //return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    public void deleteFile(final File file) {
        if (file != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteRecursive(file);
                    //finished deleting -> delete service
                    notificationManager.cancel(REX_CHANNEL_NOTI_ID);
                    sendBroadcast(new Intent("kakaoChatDelete"));
                    if(filesDeleted == totalFileCount){
                        DeleteService.this.stopSelf();
                    }
                }
            }).start();
        }
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        deletedSize += fileOrDirectory.length();
        fileOrDirectory.delete();
        filesDeleted++;
        updateNotification("요청하신 대화 백업 폴더 삭제중입니다...", "" + FileParseUtils.humanReadableByteCountBin(deletedSize) + " / " + FileParseUtils.humanReadableByteCountBin(totalSize));
    }

    void getSizeRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                getSizeRecursive(child);
        totalSize += fileOrDirectory.length();
        totalFileCount += 1;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private Notification getNotification(String titleStr, String contentStr){
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, SplashActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.kakao_ic_launcher_analyze)        // the status icon
                .setTicker(contentStr)           // the status text
                .setWhen(System.currentTimeMillis())       // the time stamp
                .setContentTitle(titleStr)                 // the label of the entry
                .setContentText(contentStr)      // the content of the entry
                .setContentIntent(contentIntent)           // the intent to send when the entry is clicked
                .setOngoing(true)                          // make persistent (disable swipe-away)
                .setChannelId(REX_CHANNEL_ID)
                .setVibrate(new long[]{0L})
                .build();
        return notification;
    }

    private void updateNotification(String title, String content) {
        String text = "Some text that will update the notification";
        Notification notification = getNotification(title, content);
        notificationManager.notify(REX_CHANNEL_NOTI_ID, notification);
    }
}