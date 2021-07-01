package com.google;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class VideoPlayer {

  private final VideoLibrary videoLibrary;
  private Video currentVideo;
  private Boolean paused = false;
  private Map<String,String> flaggedVideos = new HashMap<String,String>();
  private Set<VideoPlaylist> listOfPlaylists = new TreeSet<VideoPlaylist>(Comparator.comparing(VideoPlaylist::getName));

  public VideoPlayer() {
    this.videoLibrary = new VideoLibrary();
  }

  public void numberOfVideos() {
    System.out.printf("%s videos in the library%n", videoLibrary.getVideos().size());
  }

  public void showAllVideos() {
    System.out.println("Here's a list of all available videos:");
    List<Video> videoList = videoLibrary.getVideos();
    videoList.sort(Comparator.comparing(Video::getTitle));
    for(Video video : videoList) {
      if(flaggedVideos.containsKey(video.getVideoId())) {
        System.out.printf("%s - FLAGGED (reason: %s)%n", video.getVideoDetails(),flaggedVideos.get(video.getVideoId()));
      } else {
        System.out.printf("%s %n", video.getVideoDetails());
      }
    }
  }

  public void playVideo(String videoId) {
    List<Video> videoList = videoLibrary.getVideos();
    for(Video video : videoList) {
      if(video.getVideoId().equals(videoId)) {
        if(currentVideo != null) {
          System.out.printf("Stopping video: %s %n", currentVideo.getTitle());
        }
        String reasonFlag = videoFlagged(videoId);
        if(reasonFlag != null) {
          System.out.printf("Cannot play video: Video is currently flagged (reason: %s)%n", reasonFlag );
          return;
        }
        currentVideo = video;
        System.out.printf("Playing video: %s %n", currentVideo.getTitle());
        paused = false;
        return;
      }
    }
    System.out.println("Cannot play video: Video does not exist");
  }

  public void stopVideo() {
    if(currentVideo == null) {
      System.out.println("Cannot stop video: No video is currently playing");
    } else {
      System.out.printf("Stopping video: %s %n", currentVideo.getTitle());
      currentVideo = null;
    }
  }

  public void playRandomVideo() {
    List<Video> videoList = videoLibrary.getVideos();
    Video videoToPlay;
    Random random = new Random();
    Iterator<String> it = flaggedVideos.keySet().iterator();
    while(it.hasNext()) {
      videoList.remove(findVideo(it.next()));
    }
    if(videoList.size() == 0) {
      System.out.println("No videos available");
      return;
    }
    int randomInt = random.nextInt(videoList.size());
    videoToPlay = videoList.get(randomInt);
    playVideo(videoToPlay.getVideoId());
  }

  public void pauseVideo() {
    if(currentVideo != null) {
      if(paused == true) {
        System.out.printf("Video already paused: %s %n", currentVideo.getTitle());
      } else {
        System.out.printf("Pausing video: %s %n", currentVideo.getTitle());
        paused = true;
      }
    } else {
      System.out.println("Cannot pause video: No video is currently playing");
    }
  }

  public void continueVideo() {
    if(currentVideo != null) {
      if(paused == true) {
        System.out.printf("Continuing video: %s %n", currentVideo.getTitle());
        paused = false;
      } else {
        System.out.println("Cannot continue video: Video is not paused");
      }
    } else {
      System.out.println("Cannot continue video: No video is currently playing");
    }
  }

  public void showPlaying() {
    if(currentVideo != null) {
      if(paused == true) {
        System.out.printf("Currently playing: %s - PAUSED%n", currentVideo.getVideoDetails());
      } else {
        System.out.printf("Currently playing: %s %n", currentVideo.getVideoDetails());
      }
    } else {
      System.out.println("No video is currently playing");
    }
  }

  public void createPlaylist(String playlistName) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if (vpl == null) {
      System.out.printf("Successfully created new playlist: %s%n",playlistName);
      VideoPlaylist playlist = new VideoPlaylist(playlistName);
      listOfPlaylists.add(playlist);
    } else {
      System.out.println("Cannot create playlist: A playlist with the same name already exists");
    }
  }

  public void addVideoToPlaylist(String playlistName, String videoId) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if(vpl == null) {
      System.out.printf("Cannot add video to %s: Playlist does not exist%n",playlistName);
      return;
    } 
    Video videoExists = findVideo(videoId);
    Video videoExitsPlaylist = findVideoInPlaylist(videoId, vpl);
    if(videoExists == null) {
      System.out.printf("Cannot add video to %s: Video does not exist%n",vpl.getName());
    } else {
      String reasonFlag = videoFlagged(videoId);
      if(reasonFlag != null) {
        System.out.printf("Cannot add video to my_playlist: Video is currently flagged (reason: %s)%n", reasonFlag );
        return;
      }
      if(videoExitsPlaylist == null) {
        System.out.printf("Added video to %s: %s%n",playlistName,videoExists.getTitle());
        vpl.addVideo(videoExists);
      } else {
        System.out.printf("Cannot add video to %s: Video already added%n", playlistName);
      }
    }
  }

  public void showAllPlaylists() {
    if(listOfPlaylists.size() == 0) {
      System.out.println("No playlists exist yet");
    } else {
      System.out.println("Showing all playlists:");
      for(VideoPlaylist vpl: listOfPlaylists) {
        System.out.println(vpl.getName());
      }
    }
  }

  public void showPlaylist(String playlistName) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if(vpl == null) {
      System.out.printf("Cannot show playlist %s: Playlist does not exist%n",playlistName);
      return;
    } else {
      System.out.printf("Showing playlist: %s%n",playlistName);
      if(vpl.getVideos().size() == 0){
        System.out.println("No videos here yet");
      }
      for(Video video : vpl.getVideos()) {
        if(flaggedVideos.containsKey(video.getVideoId())) {
          System.out.printf("%s - FLAGGED (reason: %s)%n", video.getVideoDetails(),flaggedVideos.get(video.getVideoId()));
        } else {
          System.out.printf("%s %n", video.getVideoDetails());
        }
      }
    }
  }

  public void removeFromPlaylist(String playlistName, String videoId) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if(vpl == null) {
      System.out.printf("Cannot remove video from %s: Playlist does not exist%n",playlistName);
      return;
    }
    Video videoExists = findVideo(videoId);
    Video videoExitsPlaylist = findVideoInPlaylist(videoId, vpl);
    if(videoExists == null) {
      System.out.printf("Cannot remove video from %s: Video does not exist%n",playlistName);
    } else {
      if(videoExitsPlaylist == null) {
        System.out.printf("Cannot remove video from %s: Video is not in playlist%n",playlistName);
      } else {
        vpl.removeVideo(videoExists);
        System.out.printf("Removed video from %s: %s%n", playlistName,videoExists.getTitle());
      }
    }
  }

  public void clearPlaylist(String playlistName) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if(vpl  == null) {
      System.out.printf("Cannot clear playlist %s: Playlist does not exist%n", playlistName);
    } else {
      vpl.removeAllVideos();
      System.out.printf("Successfully removed all videos from %s%n", playlistName);
    }
  }

  public void deletePlaylist(String playlistName) {
    VideoPlaylist vpl = findPlayList(playlistName);
    if(vpl != null) {
      listOfPlaylists.remove(vpl);
      System.out.printf("Deleted playlist: %s%n",playlistName);
    } else {
      System.out.printf("Cannot delete playlist %s: Playlist does not exist%n",playlistName);
    }    
  }

  public void searchVideos(String searchTerm) {
    List<Video> videoList = videoLibrary.getVideos();
    videoList.sort(Comparator.comparing(Video::getTitle));
    Iterator<Video> iterator = videoList.iterator();
    List<Video> videos = new ArrayList<Video>();
    while(iterator.hasNext()) {
      Video videoToCheck = iterator.next();
      if(videoToCheck.getTitle().toLowerCase().contains(searchTerm.toLowerCase())){
        if(!flaggedVideos.containsKey(videoToCheck.getVideoId())) {
          videos.add(videoToCheck);
        }
      }
    }
    if(videos.size() == 0) {
      System.out.printf("No search results for %s%n", searchTerm);
    } else {
      System.out.printf("Here are the results for %s:%n", searchTerm);
      int i = 0;
      while(i < videos.size()) {
        System.out.printf("%s) %s%n",i+1,videos.get(i).getVideoDetails());
        i++;
      }
      System.out.println("Would you like to play any of the above? If yes, specify the number of the video.");
      System.out.println("If your answer is not a valid number, we will assume it's a no.");
      Scanner scanner= new Scanner(System.in); 
      if(scanner.hasNextInt()) {
        int place = scanner.nextInt() - 1;
        if(place < videos.size()) {
          playVideo(videos.get(place).getVideoId());
        }
      }
      scanner.close();
    }
  }

  public void searchVideosWithTag(String videoTag) {
    List<Video> videoList = videoLibrary.getVideos();
    videoList.sort(Comparator.comparing(Video::getTitle));
    Iterator<Video> iterator = videoList.iterator();
    List<Video> videos = new ArrayList<Video>();

    while(iterator.hasNext()) {
      Video videoToCheck = iterator.next();
      for(String string: videoToCheck.getTags()) {
        if(string.toLowerCase().contains(videoTag.toLowerCase())){
          if(!flaggedVideos.containsKey(videoToCheck.getVideoId())) {
            videos.add(videoToCheck);
          }
        }
      }
    }

    if(!videoTag.contains("#")) {
      System.out.printf("No search results for %s%n", videoTag);
      return;
    }

    if(videos.size() == 0) {
      System.out.printf("No search results for %s%n", videoTag);
    } else {
      System.out.printf("Here are the results for %s:%n", videoTag);
      int i = 0;
      while(i < videos.size()) {
        System.out.printf("%s) %s%n",i+1,videos.get(i).getVideoDetails());
        i++;
      }
      System.out.println("Would you like to play any of the above? If yes, specify the number of the video.");
      System.out.println("If your answer is not a valid number, we will assume it's a no.");
      Scanner scanner= new Scanner(System.in); 
      if(scanner.hasNextInt()) {
        int place = scanner.nextInt() - 1;
        if(place < videos.size()) {
          playVideo(videos.get(place).getVideoId());
        }
      }
      scanner.close();
    }
  }

  public void flagVideo(String videoId) {
    Video video = findVideo(videoId);
    if(currentVideo == video){
      stopVideo();
    }
    if(video == null) {
      System.out.println("Cannot flag video: Video does not exist");
    } else {
      flaggedVideos.put(videoId,"Not supplied");
      System.out.printf("Successfully flagged video: %s (reason: Not supplied)%n", video.getTitle());
    }
  }

  public void flagVideo(String videoId, String reason) {
    Video video = findVideo(videoId);
    if(currentVideo == video){
      stopVideo();
    }
    if(video == null) {
      System.out.println("Cannot flag video: Video does not exist");
    } else if (flaggedVideos.containsKey(videoId)) {
      System.out.println("Cannot flag video: Video is already flagged");
    } else {
      reason = reason.replace(" ", "_"); //remove white space
      flaggedVideos.put(videoId,reason);
      System.out.printf("Successfully flagged video: %s (reason: %s)%n", video.getTitle(),reason);
    }
  }

  public void allowVideo(String videoId) {
    Video video = findVideo(videoId);
    if(video == null) {
      System.out.println("Cannot remove flag from video: Video does not exist");
      return;
    }
    if(flaggedVideos.containsKey(videoId)){
      System.out.printf("Successfully removed flag from video: %s%n",video.getTitle());
    } else {
      System.out.println("Cannot remove flag from video: Video is not flagged");
    }
  }

  public VideoPlaylist findPlayList(String name) {
    Iterator<VideoPlaylist> iterator = listOfPlaylists.iterator();
    while(iterator.hasNext()) {
      VideoPlaylist vplToCheck = iterator.next();
      if(vplToCheck.getName().equalsIgnoreCase(name)) {
        return vplToCheck;
      }
    }
    return null;
  }

  public Video findVideoInPlaylist(String videoId, VideoPlaylist videoPlaylist) {
    Iterator<Video> iterator = videoPlaylist.getVideos().iterator();
    while(iterator.hasNext()) {
      Video videoToCheck = iterator.next();
      if(videoToCheck.getVideoId().equals(videoId)) {
        return videoToCheck;
      }
    }
    return null;
  }

  public Video findVideo(String videoId) {
    Iterator<Video> iterator = videoLibrary.getVideos().iterator();
    while(iterator.hasNext()) {
      Video videoToCheck = iterator.next();
      if(videoToCheck.getVideoId().equals(videoId)) {
        return videoToCheck;
      }
    }
    return null;
  }

  public String videoFlagged(String videoId) {
    return flaggedVideos.get(videoId);
  }
}