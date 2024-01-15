import javax.swing.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;



public class MusicPlayerGUI extends JFrame{
    
    // coloring     
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;
    private MusicPlayer musicPlayer;

    // enable file explorer
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist;
    private JPanel playbackBtn;
    private JSlider playbackSlider;


    public MusicPlayerGUI() {
    
        // Jframe config, set name and set height and width etc.
        super("Music Player");
        setSize(400, 600);

        // end when app closes
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // launch in the middle of screen
        setLocationRelativeTo(null);

        // disable resize of app
        setResizable(false);

        // layout = null, to control coordinates of components / set height and width
        setLayout(null);

        // change color for frame

        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();


        // default path when choosing file in file explorer
        jFileChooser.setCurrentDirectory(new File("/"));

        // filter file explorer to only see mp3
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents() {
        
        // toolbar
        addToolbar();

        // load image 
        JLabel songImage = new JLabel(loadImage("assets/record.png"));
        songImage.setBounds(0,50, getWidth() - 20, 225);
        add(songImage);

        //add song title
        songTitle = new JLabel("Song title");
        songTitle.setBounds(0, 285, getWidth()-10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        // add song artist
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);



        // slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me){
                // pause when holding 
                mouseWheelMoved(null);musicPlayer.pauseSong();
                
            }
            @Override
            public void mouseReleased(MouseEvent me) {
                // when no longer holding, resume song
                JSlider source = (JSlider) me.getSource();

                // frame value from where playback is requested by user
                int frame = source.getValue();

                // update current frame to this frame
                musicPlayer.setCurrentFrame(frame);

                // update current timer in milliseconds
                musicPlayer.setCurrentTimeinMilliseconds((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                  // resume  song
                  musicPlayer.playCurrentSong();

                  // toggle pause / toggle off play
                  enablePause();
            }
        });
        add(playbackSlider);

   // buttons for next, play etc.
        addPlaybackBtn();

        


        /*  playbackSlider.addMouseListener(new MouseAdapter() {
           @Override
            public void mousePressed(MouseEvent e) {
                // when holding, song pauses
                musicPlayer.pauseSong();
            }
            */

    }
    

    
 

    

    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        
        // make it impossible to move toolbar
        toolBar.setFloatable(false);

        // add drop down
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // add file option to menu
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        // add song menu in toolbar

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        // exit program
        JMenuItem exit = new JMenuItem("Exit program");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                System.exit(0); 
            }

        });
        fileMenu.add(exit);
        
        // about section
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 JFrame aboutPopup = new JFrame("About");
                 JLabel label = new JLabel("This is an about section");
                aboutPopup.getContentPane().add(label);
        

                aboutPopup.setSize(300, 200);
                aboutPopup.setLocationRelativeTo(null); // Center on screen
                aboutPopup.setVisible(true);
            }
        });
        fileMenu.add(about);


        // add load song in the menu 
        JMenuItem loadSong = new JMenuItem("Load song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            // int returned for action performed
            public void actionPerformed(ActionEvent e){
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                // to see if user pressed open button
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    // song object based on selected file
                    Song song = new Song(selectedFile.getPath());

                    // load in music player
                    musicPlayer.loadSong(song);

                    // update song info
                    updateSongInfo(song);

                    // update slider
                    updateSlider(song);

                    // toggle on/off

                    enablePause();
                }
            }
        });
        songMenu.add(loadSong);


        
        // add playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // add to playlist menu
        JMenuItem createPlaylist = new JMenuItem("Create playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // load music playlist
                new MusicPlaylist(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    // stop music
                    musicPlayer.stopSong();

                    // load playlist
                    musicPlayer.loadPlaylist(selectedFile);
                }   
            }
        });
        playlistMenu.add(loadPlaylist);



        add(toolBar);

    }

     
   

    private void addPlaybackBtn() {
        playbackBtn = new JPanel();
        playbackBtn.setBounds(0, 430, getWidth() - 10, 80);
        playbackBtn.setBackground(null);

        // previous
        JButton previousBtn = new JButton(loadImage("assets/previous.png"));
        previousBtn.setBorderPainted(false);
        previousBtn.setBackground(null);
        previousBtn.addActionListener(new ActionListener() {
            // to previous song
            @Override
            public void actionPerformed(ActionEvent e) {
            
            musicPlayer.previousSong();
        }
        });
        playbackBtn.add(previousBtn);
    
        // play
        JButton playBtn = new JButton(loadImage("assets/play.png"));
        playBtn.setBorderPainted(false);
        playBtn.setBackground(null);
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle off / on pause
                enablePause();

                // play / resume
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtn.add(playBtn);

        // pause
        JButton pauseBtn = new JButton(loadImage("assets/pause.png"));
        pauseBtn.setBorderPainted(false);
        pauseBtn.setBackground(null);
        pauseBtn.setVisible(false);
        pauseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle off / on
                enablePlay();

                // pause
                musicPlayer.pauseSong();

            }
        });
        playbackBtn.add(pauseBtn);
        
        // next
        JButton nextBtn = new JButton(loadImage("assets/next.png"));
        nextBtn.setBorderPainted(false);
        nextBtn.setBackground(null);
        nextBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // next song
            }
        });
        playbackBtn.add(nextBtn);

        add(playbackBtn);
        
    }

    // update slider from music player

    public void setSliderValue(int frame) {
        playbackSlider.setValue(frame);

    }

    public void updateSongInfo(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());

    }

    public void updateSlider(Song song) {
        // update max for slider
        
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        // song length label
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        // start at 00:00
        JLabel labelStart = new JLabel("00:00");
        labelStart.setFont(new Font("Dialog", Font.BOLD, 16));
        labelStart.setForeground(TEXT_COLOR);

        // end with whatever song length is
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 16));
        labelEnd.setForeground(TEXT_COLOR);
        
        labelTable.put(0, labelStart);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    
    public void enablePause(){
        // get reference to playbtn from playbackbtn panel
        JButton playButton = (JButton) playbackBtn.getComponent(1);
        JButton pauseButton = (JButton) playbackBtn.getComponent(2);

        // turn off
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // turn on (pause)
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);


    }

    public void enablePlay(){
        // get reference to playbtn from playbackbtn panel
        JButton playButton = (JButton) playbackBtn.getComponent(1);
        JButton pauseButton = (JButton) playbackBtn.getComponent(2);

        // turn on
        playButton.setVisible(true);
        playButton.setEnabled(true);

        // turn off (pause)
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
        

    }


    private ImageIcon loadImage(String imagePath) {
        try {
            // read image from path
            BufferedImage image = ImageIO.read(new File(imagePath));

            // return image to render
            return new ImageIcon(image);
        } 
        catch (Exception e){
            e.printStackTrace();
        }

        // if not found
        return null;
    }
}

    