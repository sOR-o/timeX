package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class AdvancedStopwatch extends JFrame implements ActionListener {
    private ArrayList<SwTimerPanel> swTimers = new ArrayList<>();
    private ArrayList<TimerPanel> timerButtonTimers = new ArrayList<>();
    private JButton addButton;
    private JButton addTimerButton;
    private JPanel mainPanel;
    private static final int MAX_TIMER_PANELS = 3;
    private static final int MAX_WIDTH = 420;
    private static final int MAX_HEIGHT = 390;
    private int pomodoroTime; // Variable to store the countdown time

    public AdvancedStopwatch() {
        setTitle("timeX");
        setSize(MAX_WIDTH, 60);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("+ sw");
        addButton.addActionListener(this);
        addTimerButton = new JButton("+ timer");
        addTimerButton.addActionListener(new AddTimerActionListener());
        topPanel.add(addTimerButton);
        topPanel.add(addButton);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton && swTimers.size() < MAX_TIMER_PANELS) {
            swTimers.add(new SwTimerPanel(mainPanel)); // Creating SwTimerPanel instance
            updateWindowSize();
        } else if (e.getSource() == addTimerButton && timerButtonTimers.size() < MAX_TIMER_PANELS) {
            String userInput = JOptionPane.showInputDialog(null, "Enter countdown time in minutes (e.g., 45):");
            if (userInput != null && !userInput.isEmpty()) {
                pomodoroTime = Integer.parseInt(userInput); // Store the countdown time
                TimerPanel newTimer = new TimerPanel(mainPanel, pomodoroTime);
                timerButtonTimers.add(newTimer);
                mainPanel.add(newTimer.panel); // Add the timer panel to the main panel
                updateWindowSize(); // Update window size after adding the timer panel
            }
        }
    }

    private void updateWindowSize() {
        int currentHeight = getHeight();
        int newHeight = currentHeight + 110; // Increase height by 110
        newHeight = Math.min(newHeight, MAX_HEIGHT);
        setSize(getWidth(), newHeight);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AdvancedStopwatch().setVisible(true);
            }
        });
    }

    class TimerPanel {
        private JTextField timerNameField;
        private JButton startButton, stopButton, resetButton, minusButton;
        private Timer timer;
        private long startTime;
        private long elapsedTime;
        private boolean running = false;
        private JPanel panel;

        public TimerPanel(JPanel container, int countdownTime) {
            panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            timerNameField = new JTextField("Untitled Timer", 20);
            panel.add(timerNameField, gbc);

            JLabel timeLabel = new JLabel("00:00:00", JLabel.CENTER); // Removed milliseconds display
            timeLabel.setFont(new Font("Arial", Font.BOLD, 30));
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            panel.add(timeLabel, gbc);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
            startButton = new JButton("Start");
            stopButton = new JButton("Stop");
            resetButton = new JButton("Reset");
            minusButton = new JButton("Del");

            startButton.addActionListener(this::startTimer);
            stopButton.addActionListener(this::stopTimer);
            resetButton.addActionListener(this::resetTimer);
            minusButton.addActionListener(this::removeTimer);

            buttonPanel.add(startButton);
            buttonPanel.add(stopButton);
            buttonPanel.add(resetButton);
            buttonPanel.add(minusButton);
            gbc.gridy = 2;
            panel.add(buttonPanel, gbc);

            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (running) {
                        if (elapsedTime <= 0) {
                            stopTimer(null);
                        } else {
                            elapsedTime -= 1000;
                            updateDisplay(timeLabel, elapsedTime);
                        }
                    }
                }
            });

            this.elapsedTime = countdownTime * 60 * 1000; // Set initial time to the countdown time in minutes
            updateDisplay(timeLabel, this.elapsedTime);

            container.add(panel);
        }

        private void startTimer(ActionEvent e) {
            if (!running) {
                timer.start();
                startTime = System.currentTimeMillis();
                running = true;
                startButton.setEnabled(false); // Disable Start button
                stopButton.setEnabled(true); // Enable Stop button
                updateDisplay((JLabel) panel.getComponent(1), elapsedTime); // Don't display milliseconds
            }
        }

        private void stopTimer(ActionEvent e) {
            if (running) {
                timer.stop();
                running = false;
                startButton.setEnabled(true); // Enable Start button
                stopButton.setEnabled(false); // Disable Stop button
            }
        }

        private void resetTimer(ActionEvent e) {
            stopTimer(null); // Stop the timer
            elapsedTime = pomodoroTime * 60 * 1000; // Reset time to the countdown time entered by the user
            updateDisplay((JLabel) panel.getComponent(1), elapsedTime); // Don't display milliseconds
        }

        private void removeTimer(ActionEvent e) {
            int panelCount = mainPanel.getComponentCount();
            if (panelCount <= MAX_TIMER_PANELS) {
                // Reduce window height by 110
                int currentHeight = getHeight();
                int newHeight = currentHeight - 110;
                setSize(getWidth(), newHeight);
            }
            timerButtonTimers.remove(this);
            panel.getParent().remove(panel);
            mainPanel.revalidate(); // Revalidate mainPanel
            mainPanel.repaint(); // Repaint mainPanel
        }

        private void updateDisplay(JLabel timeLabel, long elapsedTime) {
            long hours = elapsedTime / 3600000;
            long remainder = elapsedTime % 3600000;
            long minutes = remainder / 60000;
            remainder %= 60000;
            long seconds = remainder / 1000;

            String time = String.format("%02d:%02d:%02d", hours, minutes, seconds); // Display without milliseconds
            timeLabel.setText(time);
        }
    }

    class SwTimerPanel {
        private JTextField timerNameField;
        private JButton startButton, stopButton, resetButton, minusButton;
        private Timer timer;
        private long startTime;
        private long elapsedTime;
        private long swCounter = 0; // Counter for stopwatch
        private boolean running = false;
        private JPanel panel;

        public SwTimerPanel(JPanel container) {
            panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            timerNameField = new JTextField("Untitled Timer", 20);
            panel.add(timerNameField, gbc);

            JLabel timeLabel = new JLabel("00:00:00.00", JLabel.CENTER); // Display with milliseconds
            timeLabel.setFont(new Font("Arial", Font.BOLD, 30));
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            panel.add(timeLabel, gbc);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
            startButton = new JButton("Start");
            stopButton = new JButton("Stop");
            resetButton = new JButton("Reset");
            minusButton = new JButton("Del");

            startButton.addActionListener(this::startTimer);
            stopButton.addActionListener(this::stopTimer);
            resetButton.addActionListener(this::resetTimer);
            minusButton.addActionListener(this::removeTimer);

            buttonPanel.add(startButton);
            buttonPanel.add(stopButton);
            buttonPanel.add(resetButton);
            buttonPanel.add(minusButton);
            gbc.gridy = 2;
            panel.add(buttonPanel, gbc);

            timer = new Timer(1, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    long currentTime = System.currentTimeMillis();
                    elapsedTime = currentTime - startTime + swCounter; // Adjusting elapsed time with swCounter
                    updateDisplay(timeLabel, elapsedTime);
                }
            });

            this.elapsedTime = 0; // Set initial time to 0
            updateDisplay(timeLabel, this.elapsedTime);

            container.add(panel);
        }

        private void startTimer(ActionEvent e) {
            if (!running) {
                timer.start();
                startTime = System.currentTimeMillis();
                running = true;
                startButton.setEnabled(false); // Disable Start button
                stopButton.setEnabled(true); // Enable Stop button
            }
        }

        private void stopTimer(ActionEvent e) {
            if (running) {
                timer.stop();
                swCounter = elapsedTime; // Store the elapsed time when the timer is stopped
                running = false;
                startButton.setEnabled(true); // Enable Start button
                stopButton.setEnabled(false); // Disable Stop button
            }
        }

        private void resetTimer(ActionEvent e) {
            stopTimer(null); // Stop the timer
            elapsedTime = 0; // Reset time to 0
            swCounter = 0; // Reset the swCounter
            updateDisplay((JLabel) panel.getComponent(1), elapsedTime); // Display with milliseconds
        }

        private void removeTimer(ActionEvent e) {
            int panelCount = mainPanel.getComponentCount();
            if (panelCount <= MAX_TIMER_PANELS) {
                // Reduce window height by 110
                int currentHeight = getHeight();
                int newHeight = currentHeight - 110;
                setSize(getWidth(), newHeight);
            }
            swTimers.remove(this);
            panel.getParent().remove(panel);
            mainPanel.revalidate(); // Revalidate mainPanel
            mainPanel.repaint(); // Repaint mainPanel
        }

        private void updateDisplay(JLabel timeLabel, long elapsedTime) {
            long hours = elapsedTime / 3600000;
            long remainder = elapsedTime % 3600000;
            long minutes = remainder / 60000;
            remainder %= 60000;
            long seconds = remainder / 1000;
            long milliseconds = (remainder % 1000) / 10;

            String time = String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds); // Display with milliseconds
            timeLabel.setText(time);
        }
    }

    class AddTimerActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (timerButtonTimers.size() < MAX_TIMER_PANELS) {
                String userInput = JOptionPane.showInputDialog(null, "Enter countdown time in minutes (e.g., 45):");
                if (userInput != null && !userInput.isEmpty()) {
                    pomodoroTime = Integer.parseInt(userInput);
                    TimerPanel newTimer = new TimerPanel(mainPanel, pomodoroTime);
                    timerButtonTimers.add(newTimer);
                    mainPanel.add(newTimer.panel); // Add the timer panel to the main panel
                    updateWindowSize(); // Update window size after adding the timer panel
                }
            }
        }
    }
}
