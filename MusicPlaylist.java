import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlaylist extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;

    // store paths to be written to txt file when playlist loads 
    private ArrayList<String> songPaths;

    public MusicPlaylist(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
        songPaths = new ArrayList<>();

        // config dialog
        setTitle("Create Playlist");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true); 
        setLocationRelativeTo(musicPlayerGUI);

        addComponents();
    }

    private void addComponents(){
        // to hold every song path 
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth() * 0.025), 10, (int)(getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(songContainer);

        // button to add song
        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // to open file explorer
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                jFileChooser.setCurrentDirectory(new File("assets"));
                int result = jFileChooser.showOpenDialog(MusicPlaylist.this);

                File selectedFile = jFileChooser.getSelectedFile();
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    JLabel filePathLabel = new JLabel(selectedFile.getPath());
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    // add to list
                    songPaths.add(filePathLabel.getText());

                    // add to container
                    songContainer.add(filePathLabel);

                    // refreshes to show  JLabel
                    songContainer.revalidate();
                }
            }
        });
        add(addSongButton);

        // button for saving playlist 
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("assets"));
                    int result = jFileChooser.showSaveDialog(MusicPlaylist.this);

                    if(result == JFileChooser.APPROVE_OPTION){
                        // getSelectedFile() in order to reference to the file being saved
                        File selectedFile = jFileChooser.getSelectedFile();

                        // convert to .txt file unless it's already been done
                        // check if the file does not have ".txt" file extension
                        if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")){
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }

                        // create the new file 
                        selectedFile.createNewFile();

                        // write all of the song paths into this file
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        // go through song paths list + write each string into the file, each song on an a separate row
                        for(String songPath : songPaths){
                            bufferedWriter.write(songPath + "\n");
                        }
                        bufferedWriter.close();

                        JOptionPane.showMessageDialog(MusicPlaylist.this, "Playlist successfully created");
                        MusicPlaylist.this.dispose();
                    }
                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);
    }
}








