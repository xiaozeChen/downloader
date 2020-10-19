package io.demo.download;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 文件下载列表页面
 */
public class FileListFragment extends Fragment {
    private static final String TYPE = "type";

    /**
     * 下载中和失败的列表
     */
    public static final int TYPE_LOADING = 1;
    /**
     * 传输完成的列表
     */
    public static final int TYPE_FINISH = 2;

    @BindView(R.id.rvList)
    RecyclerView rvList;

    private Unbinder unbinder;
    private CopyOnWriteArrayList<FileBean> files;
    private FileListAdapter adapter;
    private int currentType;

    public static FileListFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(TYPE, type);
        FileListFragment fragment = new FileListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        init(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_list, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void init(Bundle savedInstanceState) {
        currentType = getArguments().getInt(TYPE, TYPE_LOADING);
        files = new CopyOnWriteArrayList<>();
        adapter = new FileListAdapter(files);
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (currentType == TYPE_LOADING) {
                //取消任务
                DUManager.getInstance().cancel(files.get(position).id);
            }
        });
        rvList.setLayoutManager(new LinearLayoutManager(getContext()));
        rvList.setAdapter(adapter);
        loadData(currentType);
    }

    public void loadData(int type) {
        if (type == TYPE_LOADING) {
            files.addAll(DUManager.getInstance().getTransmittingList());
        } else {
            files.addAll(DUManager.getInstance().getFinishList());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void registerEvent(FileBean event) {
        if (event != null) {
            if (currentType == TYPE_LOADING) {
                int index = -1;
                for (int i = 0; i < files.size(); i++) {
                    FileBean file = files.get(i);
                    if (file.id.equals(event.id)) {
                        index = i;
                        file.state = event.state;
                        file.progress = event.progress;
                        adapter.notifyItemChanged(i, null);
                    }
                }
                //去除列表中已完成
                if (index >= 0) {
                    if (files.get(index).state != FileType.STATE_LOADING) {
                        files.remove(index);
                        adapter.notifyItemRemoved(index);
                    }
                } else {
                    if (event.state == FileType.STATE_LOADING) {
                        files.add(event);
                        adapter.notifyDataSetChanged();
                    }
                }
            } else {
                //添加已完成任务
                if (event.state == FileType.STATE_FINISH) {
                    files.add(event);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
    }
}
