package io.demo.download;

import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.demo.download.coreprogress.ProgressHelper;
import io.demo.download.coreprogress.ProgressListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * 上传下载管理类
 */
public class DUManager {

    private static final String TAG = "DUManager";

    private static DUManager instance;
    private final OkHttpClient client;

    private final Map<String, FileBean> files;
    private final Map<String, Call> calls;

    private DUManager() {
        client = new OkHttpClient();
        files = new HashMap<>();
        calls = new HashMap<>();
    }

    public synchronized static DUManager getInstance() {
        if (instance == null) {
            instance = new DUManager();
        }
        return instance;
    }

    /**
     * 获取任务详情
     */
    public FileBean getTrans(String id) {
        return files.get(id);
    }

    /**
     * 获取正在下载中的任务列表
     */
    public List<FileBean> getTransmittingList() {
        List<FileBean> data = new ArrayList<>();
        for (FileBean file : files.values()) {
            if (file.state == FileType.STATE_LOADING) {
                data.add(file);
            }
        }
        return data;
    }

    /**
     * 获取已完成任务列表
     */
    public List<FileBean> getFinishList() {
        List<FileBean> data = new ArrayList<>();
        for (FileBean file : files.values()) {
            if (file.state == FileType.STATE_FINISH) {
                data.add(file);
            }
        }
        return data;
    }

    /**
     * 上传文件
     *
     * @param url  服务器地址
     * @param file 要上传的文件
     */
    public Observable<FileBean> addUploadFile(final String url, final File file) {
        return Observable.create(new ObservableOnSubscribe<FileBean>() {
            @Override
            public void subscribe(final ObservableEmitter<FileBean> emitter) {
                if (TextUtils.isEmpty(url) || file == null) {
                    emitter.onError(new Throwable("地址或文件不能为空"));
                }
                //添加文件下载记录
                final String fileId = MD5Util.getStringMD5(url);
                final FileBean currentFile = new FileBean(FileType.TYPE_UPLOAD, fileId, file.getName(), url, file.getPath());
                files.put(fileId, currentFile);
                MultipartBody build = new MultipartBody.Builder()
                        //设置文件类型参数
                        .addFormDataPart("test", file.getName(), RequestBody.create(null, file))
                        .build();
                Call call = client.newCall(new Request.Builder()
                        .url(url)
                        .post(ProgressHelper.withProgress(build, new ProgressListener() {
                            @Override
                            public void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e(TAG, "=============start===============");
                                Log.e(TAG, "numBytes:" + numBytes);
                                Log.e(TAG, "totalBytes:" + totalBytes);
                                Log.e(TAG, "percent:" + percent);
                                Log.e(TAG, "speed:" + speed);
                                Log.e(TAG, "progress:" + (int) (100 * percent));
                                Log.e(TAG, "============= end ===============");
                                currentFile.state = FileType.STATE_LOADING;
                                currentFile.progress = percent;
                                //发送事件
                                emitter.onNext(currentFile);
                            }

                            @Override
                            public void onProgressStart(long totalBytes) {
                                super.onProgressStart(totalBytes);
                                Log.e(TAG, "onProgressStart:" + totalBytes);
                                currentFile.state = FileType.STATE_LOADING;
                                currentFile.progress = 0d;
                                emitter.onNext(currentFile);
                            }

                            @Override
                            public void onProgressFinish() {
                                super.onProgressFinish();
                                Log.e(TAG, "onProgressFinish:");
                                currentFile.state = FileType.STATE_FINISH;
                                currentFile.progress = 1d;
                                emitter.onNext(currentFile);
                                emitter.onComplete();
                            }
                        })).build());
                //添加请求数据用于取消 call.cancel()
                calls.put(fileId, call);
                //设置请求回调
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        currentFile.state = FileType.STATE_FAIL;
                        emitter.onNext(currentFile);
                        emitter.onError(new Throwable(e));
                        Log.e(TAG, "=============onFailure===============");
                    }

                    @Override
                    public void onResponse(Call call, Response response) {
                        Log.e(TAG, "=============onResponse===============");
                        Log.e(TAG, "request headers:" + response.request().headers());
                        Log.e(TAG, "response headers:" + response.headers());
                    }
                });
            }
        }).compose(RxSchedulers.<FileBean>obIoToMain());
    }

    /**
     * 下载文件
     */
    public Observable<FileBean> addDownloadFile(final String url, final String fileName, final String path) {
        return Observable.create(new ObservableOnSubscribe<FileBean>() {
            @Override
            public void subscribe(final ObservableEmitter<FileBean> emitter) {
                if (TextUtils.isEmpty(url) || TextUtils.isEmpty(fileName)) {
                    emitter.onError(new Throwable("地址或文件名不能为空"));
                }
                //添加文件记录
                final String fileId = MD5Util.getStringMD5(fileName);
                final FileBean currentFile = new FileBean(FileType.TYPE_DOWNLOAD, fileId, fileName, url, path);
                files.put(fileId, currentFile);
                //创建请求
                Call call = client.newCall(new Request.Builder()
                        .url(url)
                        .get()
                        .build());
                calls.put(fileId, call);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e(TAG, "=============onFailure===============");
                        emitter.onNext(currentFile);
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.e(TAG, "=============onResponse===============");
                        Log.e(TAG, "request headers:" + response.request().headers());
                        Log.e(TAG, "response headers:" + response.headers());
                        BufferedSource source = ProgressHelper.withProgress(response.body(), new ProgressListener() {
                            @Override
                            public void onProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e(TAG, "=============start===============");
                                Log.e(TAG, "numBytes:" + numBytes);
                                Log.e(TAG, "totalBytes:" + totalBytes);
                                Log.e(TAG, "percent:" + percent);
                                Log.e(TAG, "speed:" + speed);
                                Log.e(TAG, "progress:" + (int) (100 * percent));
                                Log.e(TAG, "============= end ===============");
                                currentFile.state = FileType.STATE_LOADING;
                                currentFile.progress = percent;
                                emitter.onNext(currentFile);
                            }

                            @Override
                            public void onProgressStart(long totalBytes) {
                                super.onProgressStart(totalBytes);
                                Log.e(TAG, "onProgressStart:" + totalBytes);
                                currentFile.state = FileType.STATE_LOADING;
                                currentFile.progress = 0d;
                                emitter.onNext(currentFile);
                            }

                            @Override
                            public void onProgressFinish() {
                                super.onProgressFinish();
                                Log.e(TAG, "onProgressFinish:");
                                currentFile.state = FileType.STATE_FINISH;
                                currentFile.progress = 1d;
                                emitter.onNext(currentFile);
                                emitter.onComplete();
                            }
                        }).source();
                        //保存文件
                        File pathFile = new File(path);
                        if (!pathFile.exists()) {
                            pathFile.mkdirs();
                        }
                        final File outFile = new File(path, fileName);
                        if (outFile.exists()) {
                            outFile.delete();
                        } else {
                            outFile.createNewFile();
                        }
                        BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                        source.readAll(sink);
                        sink.flush();
                        source.close();
                    }
                });
            }
        }).compose(RxSchedulers.<FileBean>obIoToMain());
    }

    /**
     * 取消上传/下载
     */
    public void cancel(String id) {
        Call call = calls.get(id);
        if (call != null) {
            if (!call.isCanceled()) {
                FileBean event = files.get(id);
                if (event != null) {
                    event.state = FileType.STATE_FAIL;
                    EventBus.getDefault().post(event);
                }
                call.cancel();
            }
        }
    }

    /**
     * 取消上传/下载
     */
    public void cancelAll() {
        for (String id : calls.keySet()) {
            cancel(id);
        }
    }
}
