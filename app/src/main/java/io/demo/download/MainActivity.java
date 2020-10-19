package io.demo.download;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_FILE = 0x123;

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.etUrl)
    EditText etUrl;
    @BindView(R.id.btnDownload)
    Button btnDownload;
    @BindView(R.id.btnUpLoad)
    Button btnUpLoad;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.vpList)
    ViewPager2 vpList;

    private DUService.DUBinder duBinder;
    private ServiceConnection connn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        //简单请求权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        0x111);
            }
        }
        //绑定服务，最好在 APP 的首页,保证 APP 在运行过程中 服务不会被关闭
        connn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                duBinder = (DUService.DUBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(new Intent(this, DUService.class), connn, DUService.BIND_AUTO_CREATE);
    }

    private void initView() {
        final String[] tabNames = new String[]{"传输中", "已完成"};
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(FileListFragment.newInstance(FileListFragment.TYPE_LOADING));
        fragments.add(FileListFragment.newInstance(FileListFragment.TYPE_FINISH));
        ViewPager2Adapter adapter = new ViewPager2Adapter<>(this, fragments);
        vpList.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, vpList, (tab, position) -> {
            tab.setText(tabNames[position]);
        }).attach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connn);
    }

    @OnClick({R.id.btnDownload, R.id.btnUpLoad})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnDownload:
                if (duBinder == null) {
                    Log.e("tag", "请先开启上传/下载服务");
                    return;
                }
                //手机默认下载目录
                String path = FileHelper.getDownloadPath();
                duBinder.downloadFile(etUrl.getText().toString(), "fileName", path);
                break;
            case R.id.btnUpLoad:
                //省略权限判断
                getFileFromSys();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            //得到uri，后面就是将uri转化成file的过程。
            File file = FileHelper.getFile(this, data.getData());
            if (duBinder == null) {
                Log.e("tag", "请先开启上传/下载服务");
                return;
            }
            duBinder.uploadFile(etUrl.getText().toString(), file);
        }
    }

    /**
     * 从系统文件管理器获取文件
     */
    private void getFileFromSys() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //设置类型，这里是任意类型，
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_FILE);
    }

}
