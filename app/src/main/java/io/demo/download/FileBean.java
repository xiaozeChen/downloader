package io.demo.download;

/**
 * 下载文件信息实体类
 * 最好做成本地数据库存储，用于记录本地文件下载状态和信息
 */
public class FileBean {
    /**
     * 文件唯一索引 可自定义类型
     */
    public String id;
    public String name;
    public String url;
    public String filePath;
    public int type;
    public int state;
    public double progress;

    public FileBean(int type, String id, String name, String url, String filePath) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.url = url;
        this.filePath = filePath;
        state = FileType.STATE_LOADING;
        progress = 0.0d;
    }
}
