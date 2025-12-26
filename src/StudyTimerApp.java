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
    private static final Color TEXT_COLOR = new Color(255, 255, 255); // Bright White
    private static final Color INPUT_BG = new Color(255, 255, 255);
    // Sunset Gradient Colors (Lighter/Vibrant)
    private static final Color SUNSET_TOP = new Color(255, 95, 109); // Vibrant Pink/Coral
    private static final Color SUNSET_BOTTOM = new Color(255, 195, 113); // Lighter Orange/Gold
    private static final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 20); // Increased from 16
    private static final Font TIMER_FONT = new Font("SansSerif", Font.BOLD, 180); // Increased from 120
    private static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 24);

    // HMS Inputs
    private JTextField studyH, studyM, studyS;
    private JTextField breakH, breakM, breakS;
    private JLabel timerLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private JPanel mainPanel;

    private Timer timer;
    private int remainingSeconds;
    private boolean isStudySession = true;

    public StudyTimerApp() {
        setTitle("Study Session Planner");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Full Screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setLocationRelativeTo(null);
        // setResizable(false);
        // getContentPane().setBackground(BG_COLOR);

        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, SUNSET_TOP, 0, h, SUNSET_BOTTOM);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        // mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JLabel headerLabel = new JLabel("Study Planner");
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);

        // Study Input Row
        JPanel studyRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        studyRow.setOpaque(false);
        studyRow.add(createStyledLabel("Study Duration:  "));
        studyH = createStyledTextField("00", 2);
        studyM = createStyledTextField("25", 2);
        studyS = createStyledTextField("00", 2);
        addLabeledField(studyRow, studyH, "h");
        addLabeledField(studyRow, studyM, "m");
        addLabeledField(studyRow, studyS, "s");

        // Break Input Row
        JPanel breakRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        breakRow.setOpaque(false);
        breakRow.add(createStyledLabel("Break Duration:  "));
        breakH = createStyledTextField("00", 2);
        breakM = createStyledTextField("05", 2);
        breakS = createStyledTextField("00", 2);
        addLabeledField(breakRow, breakH, "h");
        addLabeledField(breakRow, breakM, "m");
        addLabeledField(breakRow, breakS, "s");

        inputPanel.add(studyRow);
        inputPanel.add(breakRow);

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
            if (startButton.getText().equals("Start Session")) {
                remainingSeconds = getDuration(studyH, studyM, studyS);
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

                remainingSeconds = getDuration(breakH, breakM, breakS);
                updateStatus(false);
                timer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Break Finished -> Loop back to Study
            try {
                // Alarm sound (beep beep beep)
                for (int i = 0; i < 3; i++) {
                    playTone(1000, 200);
                    Thread.sleep(100);
                }

                // Switch back to Study
                isStudySession = true;
                remainingSeconds = getDuration(studyH, studyM, studyS);
                updateStatus(true);
                timer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Audio Synthesis Helper
    private void playTone(int hz, int msecs) throws javax.sound.sampled.LineUnavailableException {
        float SAMPLE_RATE = 8000f;
        byte[] buf = new byte[1];
        javax.sound.sampled.AudioFormat af = new javax.sound.sampled.AudioFormat(
                SAMPLE_RATE, // sampleRate
                8, // sampleSizeInBits
                1, // channels
                true, // signed
                false); // bigEndian
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
            // getContentPane().setBackground(BG_COLOR);
            // mainPanel.setBackground(BG_COLOR); // Keep main BG clean
            timerLabel.setForeground(STUDY_COLOR.darker());
        } else {
            statusLabel.setText("Enjoy your Break!");
            // Optional: Change BG slightly or just text color to indicate break
            timerLabel.setForeground(BREAK_COLOR.darker());
        }
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        if (hours > 0) {
            timerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private int getDuration(JTextField h, JTextField m, JTextField s) {
        int hours = parseSafe(h.getText());
        int mins = parseSafe(m.getText());
        int secs = parseSafe(s.getText());
        return (hours * 3600) + (mins * 60) + secs;
    }

    private int parseSafe(String text) {
        try {
            return text.trim().isEmpty() ? 0 : Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void addLabeledField(JPanel p, JTextField tf, String label) {
        p.add(tf);
        JLabel l = createStyledLabel(label);
        l.setFont(l.getFont().deriveFont(12f));
        p.add(l);
        p.add(Box.createRigidArea(new Dimension(10, 0)));
    }

    // Helper Components
    private JLabel createStyledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MAIN_FONT);
        l.setForeground(TEXT_COLOR);
        return l;
    }

    private JTextField createStyledTextField(String defaultText, int columns) {
        JTextField tf = new JTextField(defaultText, columns);
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setFont(MAIN_FONT);
        tf.setForeground(Color.BLACK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
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
