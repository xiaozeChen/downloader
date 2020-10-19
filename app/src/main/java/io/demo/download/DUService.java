package io.demo.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 上传下载服务，需绑定到APP生命周期相等的页面
 */
public class DUService extends Service {

    private DUBinder binder;

    public class DUBinder extends Binder {

        /**
         * 取消下载或者上传任务
         */
        public void cancelTask(String id) {
            DUManager.getInstance().cancel(id);
        }

        public void downloadFile(String url, String fileName, String path) {
            //创建 通知内容
            final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel("DuService", "DuService", NotificationManager.IMPORTANCE_LOW);
                mChannel.setDescription("Morecoin");
                mChannel.setShowBadge(false);
                manager.createNotificationChannel(mChannel);
            }
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(DUService.this, "DuService");
            //设置点击后跳转意图
            Intent noticeIntent = new Intent(DUService.this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(DUService.this, 0, noticeIntent, 0);
            builder.setContentIntent(pendingIntent);

            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            builder.setContentTitle("正在下载" + fileName);
            builder.setTicker("正在下载中");
            builder.setContentText(String.format("下载进度:%1$d%%/100%%", 0));
            manager.notify(100, builder.build());

            DUManager.getInstance()
                    .addDownloadFile(url, fileName, path)
                    .subscribe(new Observer<FileBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(FileBean data) {
                            if (data.state == FileType.STATE_LOADING) {
                                builder.setContentText(String.format("下载进度:%1$d%%/100%%", (int) (data.progress * 100)));
                                builder.setProgress(100, (int) (data.progress * 100), false);
                            } else if (data.state == FileType.STATE_FINISH) {
                                builder.setContentText("下载完成");
                                builder.setProgress(100, 100, false);
                            } else if (data.state == FileType.STATE_FAIL) {
                                builder.setContentText("下载失败");
                                builder.setProgress(100, 0, false);
                            }
                            manager.notify(0, builder.build());
                            //发送事件
                            EventBus.getDefault().post(data);
                        }

                        @Override
                        public void onError(Throwable e) {
                            builder.setContentText("下载失败");
                            builder.setProgress(100, 0, false);
                            manager.notify(0, builder.build());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        public void uploadFile(String url, File file) {
            //创建 通知内容
            final NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel("DUService",
                        "DuService", NotificationManager.IMPORTANCE_LOW);
                mChannel.setDescription("DUService");
                mChannel.setShowBadge(false);
                manager.createNotificationChannel(mChannel);
            }
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(DUService.this, "DUService");
            //设置点击后跳转意图
            Intent noticeIntent = new Intent(DUService.this, MainActivity.class);
            noticeIntent.putExtra("isRetry", false);
            PendingIntent pendingIntent = PendingIntent.getActivity(DUService.this, 0, noticeIntent, 0);
            builder.setContentIntent(pendingIntent);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            builder.setContentTitle("正在上传" + file.getName());
            builder.setTicker("正在上传中");
            builder.setContentText(String.format("上传进度:%1$d%%/100%%", 0));
            Notification notification = builder.build();
            manager.notify(0, notification);

            DUManager.getInstance()
                    .addUploadFile(url, file)
                    .subscribe(new Observer<FileBean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(FileBean data) {
                            if (data.state == FileType.STATE_LOADING) {
                                builder.setContentText(String.format("上传进度:%1$d%%/100%%", (int) (data.progress * 100)));
                                builder.setProgress(100, (int) (data.progress * 100), false);
                            } else if (data.state == FileType.STATE_FINISH) {
                                builder.setContentText("上传完成");
                                builder.setProgress(100, 100, false);
                            } else if (data.state == FileType.STATE_FAIL) {
                                builder.setContentText("上传失败");
                                builder.setProgress(100, 0, false);
                            }
                            manager.notify(0, builder.build());
                            //发送事件 用于更新列表
                            EventBus.getDefault().post(data);
                        }

                        @Override
                        public void onError(Throwable e) {
                            builder.setContentText("上传失败");
                            builder.setProgress(100, 0, false);
                            manager.notify(0, builder.build());
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null) {
            binder = new DUBinder();
        }
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //取消所有下载
        DUManager.getInstance().cancelAll();
        return super.onUnbind(intent);
    }
}
