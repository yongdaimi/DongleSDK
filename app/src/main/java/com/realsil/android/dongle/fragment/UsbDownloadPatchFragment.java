package com.realsil.android.dongle.fragment;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.realsil.android.dongle.R;
import com.realsil.android.dongle.adapter.UsbDebugMsgListAdapter;
import com.realsil.android.dongle.base.BaseFragment;
import com.realsil.android.dongle.entity.UsbMsg;
import com.realsil.android.dongle.util.FileUtil;

import java.util.ArrayList;


public class UsbDownloadPatchFragment extends BaseFragment implements View.OnClickListener {


    private Button btn_load_patch_code;
    private Button btn_load_config_file;
    private Button btn_start_download_patch;

    private RecyclerView rv_msg_list;

    private UsbDebugMsgListAdapter mUsbDebugMsgListAdapter;

    private static final int REQUEST_CODE_CHOOSE_PATCH_CODE_FILE = 10001;
    private static final int REQUEST_CODE_CHOOSE_CONFIG_FILE     = 10002;

    private static final String TAG = "xp.chen";

    private String mSelectedPathCodePath;
    private String mSelectedConfigFilePath;

    @Override
    protected void setContainer() {
        setContentView(R.layout.fragment_usb_download_patch);
    }

    @Override
    protected void init() {
        btn_load_patch_code = findViewById(R.id.btn_load_patch_code);
        btn_load_config_file = findViewById(R.id.btn_load_config_file);
        btn_start_download_patch = findViewById(R.id.btn_start_download_patch);

        rv_msg_list = findViewById(R.id.rv_msg_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv_msg_list.setLayoutManager(layoutManager);
        rv_msg_list.setHasFixedSize(true);
    }

    @Override
    protected void setListener() {
        btn_load_patch_code.setOnClickListener(this);
        btn_load_config_file.setOnClickListener(this);
        btn_start_download_patch.setOnClickListener(this);
    }


    public static UsbDownloadPatchFragment newInstance() {
        UsbDownloadPatchFragment mFragment = new UsbDownloadPatchFragment();
        Bundle args = new Bundle();
        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUsbDebugMsgListAdapter = new UsbDebugMsgListAdapter(mContext, new ArrayList<UsbMsg>());
        rv_msg_list.setAdapter(mUsbDebugMsgListAdapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_load_patch_code:
                loadPatchCode();
                break;
            case R.id.btn_load_config_file:
                loadConfigFile();
                break;
            case R.id.btn_start_download_patch:
                startDownloadPatch();
                break;
            default:
                break;
        }
    }


    private void loadPatchCode() {
        chooseFile(REQUEST_CODE_CHOOSE_PATCH_CODE_FILE);
    }

    private void loadConfigFile() {
        chooseFile(REQUEST_CODE_CHOOSE_CONFIG_FILE);
    }

    private void startDownloadPatch() {
        if (TextUtils.isEmpty(mSelectedPathCodePath)) {
            sendMessage(getLocalString(R.string.usb_error_no_patch_code_file), UsbMsg.MSG_TYPE_ERROR);
            return;
        }

        if (TextUtils.isEmpty(mSelectedConfigFilePath)) {
            sendMessage(getLocalString(R.string.usb_error_no_config_file), UsbMsg.MSG_TYPE_ERROR);
            return;
        }

        sendMessage("Found the patch code file and config file");


    }

    private void chooseFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, requestCode);
    }


    private void sendMessage(String message) {
        sendMessage(message, UsbMsg.MSG_TYPE_NORMAL);
    }

    private void sendMessage(String message, int errorCode) {
        UsbMsg usbMsg = new UsbMsg(message, errorCode);
        Message msg = mHandler.obtainMessage();
        msg.obj = usbMsg;
        msg.sendToTarget();
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            UsbMsg usbMsg = (UsbMsg) msg.obj;
            mUsbDebugMsgListAdapter.addMsgItem(usbMsg);
            mUsbDebugMsgListAdapter.notifyItemInserted(0);
            rv_msg_list.scrollToPosition(0);
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE_PATCH_CODE_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) return;
            Uri uri = data.getData();
            Log.i(TAG, "File uri: " + uri.toString());

            mSelectedPathCodePath = FileUtil.getFilePath(mContext, uri);
            Log.i(TAG, "filePath: " + mSelectedPathCodePath);
            sendMessage("Select Patch Code Path: " + mSelectedPathCodePath);
        }

        if (requestCode == REQUEST_CODE_CHOOSE_CONFIG_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) return;
            Uri uri = data.getData();
            Log.i(TAG, "File uri: " + uri.toString());

            mSelectedConfigFilePath = FileUtil.getFilePath(mContext, uri);
            Log.i(TAG, "filePath: " + mSelectedConfigFilePath);
            sendMessage("Select Patch Code Path: " + mSelectedConfigFilePath);
        }
    }

}