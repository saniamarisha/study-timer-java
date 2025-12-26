import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class StudyTimerApp extends JFrame {

    // Pastel Color Palette
    private static final Color BG_COLOR = new Color(253, 252, 240); // Cream/Off-white
    private static final Color STUDY_COLOR = new Color(181, 234, 215); // Pastel Green
    private static final Color BREAK_COLOR = new Color(255, 183, 178); // Pastel Pink
    private static final Color TEXT_COLOR = new Color(85, 85, 85); // Dark Gray
    private static final Color INPUT_BG = new Color(255, 255, 255);
    private static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font TIMER_FONT = new Font("SansSerif", Font.BOLD, 48);
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 24);

    private JTextField studyInput;
    private JTextField breakInput;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private JPanel mainPanel;

    private Timer timer;
    private int remainingSeconds;
    private boolean isStudySession = true;

    public StudyTimerApp() {
        setTitle("Study Session Planner");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_COLOR);

        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("Study Planner");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setMaximumSize(new Dimension(300, 80));
        
        JLabel studyLabel = createStyledLabel("Study (min):");
        JLabel breakLabel = createStyledLabel("Break (min):");
        
        studyInput = createStyledTextField("25");
        breakInput = createStyledTextField("5");

        inputPanel.add(studyLabel);
        inputPanel.add(studyInput);
        inputPanel.add(breakLabel);
        inputPanel.add(breakInput);

        // Timer Display
        timerLabel = new JLabel("00:00");
        timerLabel.setFont(TIMER_FONT);
        timerLabel.setForeground(TEXT_COLOR);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        statusLabel = new JLabel("Ready to Focus?");
        statusLabel.setFont(MAIN_FONT);
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Start Button
        startButton = new RoundedButton("Start Session");
        startButton.setFont(MAIN_FONT);
        startButton.setBackground(STUDY_COLOR);
        startButton.setForeground(TEXT_COLOR);
        startButton.setFocusPainted(false);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> startSession());
        
        // Add components with spacing
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(timerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        mainPanel.add(startButton);

        add(mainPanel);
    }

    private void startSession() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            startButton.setText("Start Session");
            statusLabel.setText("Paused");
            return;
        }

        try {
            int studyMin = Integer.parseInt(studyInput.getText());
            int breakMin = Integer.parseInt(breakInput.getText());
            
            if (startButton.getText().equals("Start Session")) {
                remainingSeconds = studyMin * 60;
                isStudySession = true;
                updateStatus(true);
            }
            
            startTimerIter();
            startButton.setText("Stop");
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for time.");
        }
    }

    private void startTimerIter() {
         timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingSeconds--;
                updateTimerDisplay();

                if (remainingSeconds <= 0) {
                   switchSession();
                }
            }
        });
        timer.start();
    }

    private void switchSession() {
        timer.stop();
        if (isStudySession) {
            // Study Finished -> Start Break
            isStudySession = false;
            try {
                playTone(600, 300); // Simple 'ding'
                Thread.sleep(100);
                playTone(800, 500); // Higher 'dong'
                
                int breakMin = Integer.parseInt(breakInput.getText());
                remainingSeconds = breakMin * 60;
                updateStatus(false);
                timer.start();
            } catch (Exception e) {
                 e.printStackTrace();
            }
        } else {
            // Break Finished -> Session Done
            try {
                // Alarm sound (beep beep beep)
                for(int i=0; i<3; i++) {
                     playTone(1000, 200);
                     Thread.sleep(100);
                }
            } catch (Exception e) {}
            
            statusLabel.setText("Session Complete!");
            startButton.setText("Start Session");
            timerLabel.setText("00:00");
        }
    }

    // Audio Synthesis Helper
    private void playTone(int hz, int msecs) throws javax.sound.sampled.LineUnavailableException {
        float SAMPLE_RATE = 8000f;
        byte[] buf = new byte[1];
        javax.sound.sampled.AudioFormat af = 
            new javax.sound.sampled.AudioFormat(
                SAMPLE_RATE, // sampleRate
                8,           // sampleSizeInBits
                1,           // channels
                true,        // signed
                false);      // bigEndian
        javax.sound.sampled.SourceDataLine sdl = javax.sound.sampled.AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i = 0; i < msecs * 8; i++) {
            double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
            buf[0] = (byte) (Math.sin(angle) * 127.0 * 0.8); // 0.8 volume
            sdl.write(buf, 0, 1);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    private void updateStatus(boolean studying) {
        if (studying) {
            statusLabel.setText("Currently Studying...");
            getContentPane().setBackground(BG_COLOR);
            mainPanel.setBackground(BG_COLOR); // Keep main BG clean
            timerLabel.setForeground(STUDY_COLOR.darker());
        } else {
            statusLabel.setText("Enjoy your Break!");
            // Optional: Change BG slightly or just text color to indicate break
            timerLabel.setForeground(BREAK_COLOR.darker());
        }
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // Helper Components
    private JLabel createStyledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MAIN_FONT);
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JTextField createStyledTextField(String defaultText) {
        JTextField tf = new JTextField(defaultText);
        tf.setFont(MAIN_FONT);
        tf.setForeground(TEXT_COLOR);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        return tf;
    }

    // Custom Rounded Button
    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            new StudyTimerApp().setVisible(true);
        });
    }
}
