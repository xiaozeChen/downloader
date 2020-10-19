package io.demo.download;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 文件管理类
 */
public class FileHelper {

    public static String getDownloadPath() {
        String appCachePath = Utils.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = new File(appCachePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return appCachePath;
    }

    /**
     * 根据uri获取文件
     */
    public static File getFile(Context context, Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = Utils.getContext()
                    .getContentResolver()
                    .openFileDescriptor(uri, "r");
            FileDescriptor fd = fileDescriptor.getFileDescriptor();
            File fileDir = Utils.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            File file = new File(fileDir + "/" + SystemClock.currentThreadTimeMillis() + "." +
                    context.getContentResolver().getType(uri));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileInputStream fileInputStream = new FileInputStream(fd);
            // 设置数据缓冲
            byte[] bs = new byte[2048];
            // 读取到的数据长度
            int len;
            while ((len = fileInputStream.read(bs)) != -1) {
                fileOutputStream.write(bs, 0, len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
