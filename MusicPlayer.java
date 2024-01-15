import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {

    // update ispaused synchronously 
    private static final Object playSignal = new Object();

    // gui reference
    private MusicPlayerGUI musicPlayerGUI;
    
    // store song details
    private Song currentSong;
    public Song getCurrentSong() {
     return currentSong;
    }

    private ArrayList<Song> playlist;

    // to keep track of index of playlist
    private int currentPlaylistIndex;

    
    // pause boolean
    private boolean isPaused;

    
    // Jlayer lib for advanced player object to play music files
    private AdvancedPlayer advPlayer;

    // boolean: tell if the song has finished
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;
 
    // stores lost frame when playback is finished
    private int currentFrame;
    public void setCurrentFrame(int frame){
        currentFrame = frame;
    }

    // keep track of how many milliseconds passed since playing (to update slider)
    private int currentTimeinMilliseconds;
    public void setCurrentTimeinMilliseconds(int timeinMilliseconds){
        currentTimeinMilliseconds = timeinMilliseconds;
    }

    // constructor for music player
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        
    }

    public void loadSong(Song song){
        currentSong = song;
        playlist = null;

        // stop the song if possible
        if(!songFinished)
            stopSong();

        // play the current song if not null
        if(currentSong != null){
            // reset frame
            currentFrame = 0;

            // reset current time in milliseconds
            currentTimeinMilliseconds = 0;

            // update gui
            musicPlayerGUI.setSliderValue(0);

            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        // store paths from text file into arraylist 
        try{
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String songPath;
            while((songPath = bufferedReader.readLine()) != null){
                // create  object based on  path
                Song song = new Song(songPath);

                // add to array list
                playlist.add(song);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(playlist.size() > 0){
            // reset slider
            musicPlayerGUI.setSliderValue(0);
            currentTimeinMilliseconds = 0;

            // update current song to first in list
            currentSong = playlist.get(0);

            // start from the beginning
            currentFrame = 0;

            // update gui
            musicPlayerGUI.enablePause();
            musicPlayerGUI.updateSongInfo(currentSong);
            musicPlayerGUI.updateSlider(currentSong);

            // start song
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if(advPlayer != null) {
            // is paused:
            isPaused = true;
            // stop player
            stopSong();
        }
    }

    public void stopSong() {
        if(advPlayer != null) {
            advPlayer.stop();
            advPlayer.close();
            advPlayer = null;            
        }
    }

    public void nextSong() {
        // if there is no playlist, there is no next song
        if(playlist == null) return;

        // check if end of playlist is reached
        if(currentPlaylistIndex +1> playlist.size() -1) return;

        pressedNext = true;

        
        // stop song
        if(!songFinished)
            stopSong();

        // current playlist index increases:
        currentPlaylistIndex++;

        // current song update
        currentSong = playlist.get(currentPlaylistIndex);

        // frame reset
        currentFrame = 0;

        // reset time (milliseconds)
        currentTimeinMilliseconds = 0;
        // gui update

        musicPlayerGUI.enablePause();
        musicPlayerGUI.updateSongInfo(currentSong);
        musicPlayerGUI.updateSlider(currentSong);
        
        // play
        playCurrentSong();
       
    }

    public void previousSong() {
              // if there is no playlist, there is no next song
        if(playlist == null) return;

        // check if it is possible to go to previous song
        if(currentPlaylistIndex - 1 < 0) return;
        pressedPrev = true;

        // stop song
        if(!songFinished)
            stopSong();

        // current playlist index decreases:
        currentPlaylistIndex--;

        // current song update
        currentSong = playlist.get(currentPlaylistIndex);

        // frame reset
        currentFrame = 0;

        // reset time (milliseconds)
        currentTimeinMilliseconds = 0;

        // gui update
        musicPlayerGUI.enablePause();
        musicPlayerGUI.updateSongInfo(currentSong);
        musicPlayerGUI.updateSlider(currentSong);
        
        // play
        playCurrentSong();
    }


    public void playCurrentSong() {

        if(currentSong == null) return;


        try {
            // retrieve mp3 audio data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // new advanced player
            advPlayer = new AdvancedPlayer(bufferedInputStream);
            advPlayer.setPlayBackListener(this);
            // start music
            startMusicThread();

            // start  slider thread
            startSliderThread();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // thread to handle music
    private void startMusicThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(isPaused){
                        synchronized(playSignal){
                            // update paused
                            isPaused = false;

                            // notify to continue
                            playSignal.notify();
                        }

                        // resume 
                        advPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else{
                        // play from start
                        advPlayer.play();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

        // thread to handle slider update
        private void startSliderThread(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(isPaused){
                        try{
                            // wait until notified to continue
                            
                            synchronized(playSignal){
                                playSignal.wait();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
    
                    while(!isPaused && !songFinished && !pressedNext && !pressedPrev){
                        try{
                            // increment time with mmilliseconds
                            currentTimeinMilliseconds++;
    
                            // calculate frame value
                            int calculatedFrame = (int) ((double) currentTimeinMilliseconds * 2.08 * currentSong.getFrameRatePerMilliseconds());
    
                            // GUI update
                            musicPlayerGUI.setSliderValue(calculatedFrame);
    
                            // mimic one milliseconds by using thread.sleep
                            Thread.sleep(1);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        @Override
        public void playbackStarted(PlaybackEvent evt) {
            // calling method in the beginning of song
            System.out.println("Playback Started");
            songFinished = false;
            pressedNext = false;
            pressedPrev = false;
        }
    
        @Override
        public void playbackFinished(PlaybackEvent evt) {
            // calling method in end of song
            System.out.println("Playback Finished");
            if(isPaused){
                currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
            }else{
                // if next or previous is pressed, do not execute
                if(pressedNext || pressedPrev) return;
    
                // when the song ends
                songFinished = true;
    
                if(playlist == null){
                    // update gui
                    musicPlayerGUI.enablePlay();
                }else{
                    // for last song in list
                    if(currentPlaylistIndex == playlist.size() - 1){
                        // update gui
                        musicPlayerGUI.enablePlay();
                    }else{
                        // next song in list
                        nextSong();
                    }
                }
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    