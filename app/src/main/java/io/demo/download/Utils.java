package io.demo.download;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * 常用工具类
 */
public final class Utils {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(@NonNull final Context context) {
        Utils.context = context;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }
        throw new NullPointerException("should be initialized in application");
    }

    public static int getColor(int res) {
        if (context != null) {
            return ContextCompat.getColor(context, res);
        }
        return Color.WHITE;
    }

    public static Drawable getDrawable(int res) {
        if (context != null) {
            return ContextCompat.getDrawable(context, res);
        }
        return null;
    }

    public static String getString(int resId) {
        if (context != null) {
            return context.getString(resId);
        }
        return "";
    }

    public static String[] getStringArray(int resId) {
        if (context != null) {
            return context.getResources().getStringArray(resId);
        }
        return null;
    }
}
