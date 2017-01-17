package com.mpos.sdk;

/**
 * Created by chenld on 2016/12/29.
 */

public interface IProgressListener {
    public void onDownloadSize(int type, int step, int doneFlag, int fileCount, int curFile,
                               int total, int cur, int curFileSize, int status);
}
