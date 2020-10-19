package io.demo.download;

/**
 * 文件状态
 */
public class FileType {

    public static final int TYPE_UPLOAD = 0x100;
    public static final int TYPE_DOWNLOAD = 0x101;

    public static final int STATE_LOADING = 0x200;
    public static final int STATE_FINISH = 0x201;
    public static final int STATE_FAIL = 0x202;
    public static final int STATE_DELETED = 0x203;

    /**
     * 获取下载文件状态
     */
    public static String getFileStateStr(int type, int state) {
        if (type == TYPE_DOWNLOAD) {
            switch (state) {
                case STATE_FINISH:
                    return "已完成";
                case STATE_FAIL:
                    return "下载失败";
                case STATE_DELETED:
                    return "已删除";
                default:
                    return "下载中";
            }
        } else {
            switch (state) {
                case STATE_FINISH:
                    return "已完成";
                case STATE_FAIL:
                    return "上传失败";
                case STATE_DELETED:
                    return "已删除";
                default:
                    return "上传中";
            }
        }
    }
}
