package com.shopelia.android.app.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.shopelia.android.utils.IOUtils;

final class UUIDManager {

    public interface OnReceiveUuidListener {
        public void onReceiveUuid(String uuid);
    }

    private static final String DIRECTORY = "Shopelia/";
    private static final String SAVE_FILE = "uuid";
    private static final String CHARSET = "UTF-8";

    private static UUIDManager sInstance;

    private String mUuid;

    private UUIDManager() {

    }

    private static UUIDManager getInstance() {
        return sInstance == null ? sInstance = new UUIDManager() : sInstance;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public static void ObtainUuidTask(OnReceiveUuidListener listener) {
        UUIDManager manager = getInstance();
        if (manager.mUuid != null) {
            listener.onReceiveUuid(manager.mUuid);
        } else {
            new RetrieveUuid(listener).execute();
        }
    }

    private static class RetrieveUuid extends AsyncTask<Void, Void, String> {

        private OnReceiveUuidListener mListener;

        public RetrieveUuid(OnReceiveUuidListener listener) {
            mListener = listener;
        }

        @Override
        protected String doInBackground(Void... params) {
            File uuidFile = new File(new File(Environment.getExternalStorageDirectory(), DIRECTORY), SAVE_FILE);
            String uuid = null;

            if (uuidFile.exists()) {
                try {
                    StringWriter writer = new StringWriter();
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(uuidFile), CHARSET);
                    IOUtils.copy(reader, writer);
                } catch (Exception e) {

                }
            }

            if (TextUtils.isEmpty(uuid)) {
                uuid = createUuid();
                saveUuid(uuid);
            }
            return uuid;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            getInstance().setUuid(result);
            mListener.onReceiveUuid(result);
        }

        private String createUuid() {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 32).toLowerCase();
        }

        private void saveUuid(String uuid) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory(), DIRECTORY);
                dir.mkdirs();
                StringReader reader = new StringReader(uuid);
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(dir, SAVE_FILE)), CHARSET);
                IOUtils.copy(reader, writer);
            } catch (IOException e) {

            }
        }

    }

}
