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
        setTitle("Pastel Study Timer");
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
            
            // Initial Start logic
            if (startButton.getText().equals("Start Session")) {
                remainingSeconds = studyMin * 60;
                isStudySession = true;
                updateStatus(true);
            }
            // else resuming... (simplified for this version, we just restart or pause/resume logic can be added, 
            // but requirements say "Clicks Start Session -> Starts countdown", so let's keep it simple restart/start mechanism usually expected in basic apps, 
            // OR fully implement pause. The button text toggle implies pause/resume capability or restart.
            // Let's go with: If running -> Pause. If Paused -> Resume. If "Start Session" -> New Session.)
            
            // Actually, let's strictly follow: "Clicks Start Session -> App Starts". Simple.
            // I'll make the button toggle between Start and Stop for simplicity of code in this constraint.
            
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
            // Switch to Break
            isStudySession = false;
            try {
                int breakMin = Integer.parseInt(breakInput.getText());
                remainingSeconds = breakMin * 60;
                updateStatus(false);
                timer.start();
                Toolkit.getDefaultToolkit().beep(); // Alert
            } catch (NumberFormatException e) {
                 // Should not happen if validated start
            }
        } else {
            // Session Complete
            statusLabel.setText("Session Complete!");
            startButton.setText("Start Session");
            timerLabel.setText("00:00");
             Toolkit.getDefaultToolkit().beep(); 
        }
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
