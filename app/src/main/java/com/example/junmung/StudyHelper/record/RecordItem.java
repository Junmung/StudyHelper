package com.example.junmung.studyhelper.record;

import android.media.MediaMetadataRetriever;

public class RecordItem {
    private String title;
    private String savedDate;
    private String playTime;
    private String filePath;

    public RecordItem(String title, String savedDate, String filePath) {
        this.title = title;
        this.savedDate = savedDate;
        this.filePath = filePath;
        this.playTime = getFileDuration(filePath);
    }

    public String getTitle() {
        return title;
    }

    public String getSavedDate() {
        return savedDate;
    }

    public String getPlayTime(){
        return playTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileDuration(String filePath){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        return duration;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
