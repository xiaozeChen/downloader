package io.demo.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 下载文件适配器
 */
public class FileListAdapter extends BaseQuickAdapter<FileBean, BaseViewHolder> {

    public FileListAdapter(@Nullable List<FileBean> data) {
        super(R.layout.item_file_list, data);
    }

    @Override
    protected void convertPayloads(@NonNull BaseViewHolder helper, FileBean item, @NonNull List<Object> payloads) {
        super.convertPayloads(helper, item, payloads);
        helper.setText(R.id.tvState, FileType.getFileStateStr(item.type, item.state))
                .setGone(R.id.btnDelete, item.state == FileType.STATE_FINISH ||
                        item.state == FileType.STATE_LOADING)
                .setProgress(R.id.pb, (int) (item.progress * 100))
                .addOnClickListener(R.id.btnDelete);
        if (item.state == FileType.STATE_FINISH) {
            helper.setText(R.id.btnDelete, "删除");
        } else if (item.state == FileType.STATE_LOADING) {
            helper.setText(R.id.btnDelete, "取消");
        }
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, FileBean item) {
        helper.setText(R.id.tvName, item.name)
                .setText(R.id.tvState, FileType.getFileStateStr(item.type, item.state))
                .setGone(R.id.btnDelete, item.state == FileType.STATE_FINISH ||
                        item.state == FileType.STATE_LOADING)
                .setProgress(R.id.pb, (int) (item.progress * 100))
                .addOnClickListener(R.id.btnDelete);
        if (item.state == FileType.STATE_FINISH) {
            helper.setText(R.id.btnDelete, "删除");
        } else if (item.state == FileType.STATE_LOADING) {
            helper.setText(R.id.btnDelete, "取消");
        }
    }
}
