package com.google;

import java.util.ArrayList;
import java.util.List;

/** A class used to represent a Playlist */
class VideoPlaylist {
    List<Video> playList = new ArrayList<Video>();
    String name;

    VideoPlaylist(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    List<Video> getVideos() {
        return playList;
    }

    void addVideo(Video video) {
        playList.add(video);
    }

    void removeVideo(Video video) {
        playList.remove(video);
    }

    void removeAllVideos() {
        playList.clear();
    }
}
