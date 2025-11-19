import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ErrorOverlay {
    private static JFrame frame;
    private static final Color ERROR_BG = new Color(40, 40, 40);
    private static final Color ERROR_HEADER_BG = new Color(220, 38, 38);
    private static final Color ERROR_TEXT = new Color(252, 165, 165);
    private static final Color ERROR_TITLE = Color.WHITE;

    public static void show(String title, String message, String details) {
        SwingUtilities.invokeLater(() -> {
            if (frame != null && frame.isVisible()) {
                frame.dispose();
            }

            frame = new JFrame("Build Error");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setUndecorated(false);
            frame.setAlwaysOnTop(true);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(ERROR_BG);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(ERROR_HEADER_BG);
            header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel titleLabel = new JLabel("⚠ " + title);
            titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
            titleLabel.setForeground(ERROR_TITLE);
            header.add(titleLabel, BorderLayout.WEST);

            JLabel closeHint = new JLabel("Press ESC to close");
            closeHint.setFont(new Font("Monospaced", Font.PLAIN, 12));
            closeHint.setForeground(new Color(255, 255, 255, 180));
            header.add(closeHint, BorderLayout.EAST);

            mainPanel.add(header, BorderLayout.NORTH);

            // Content
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(ERROR_BG);
            content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

            JLabel messageLabel = new JLabel("<html><body style='width: 600px'>" + 
                message.replace("\n", "<br>") + "</body></html>");
            messageLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
            messageLabel.setForeground(ERROR_TEXT);
            content.add(messageLabel, BorderLayout.NORTH);

            if (details != null && !details.trim().isEmpty()) {
                JTextArea detailsArea = new JTextArea(details);
                detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                detailsArea.setBackground(new Color(30, 30, 30));
                detailsArea.setForeground(new Color(220, 220, 220));
                detailsArea.setCaretColor(Color.WHITE);
                detailsArea.setEditable(false);
                detailsArea.setLineWrap(false);
                detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JScrollPane scrollPane = new JScrollPane(detailsArea);
                scrollPane.setPreferredSize(new Dimension(700, 300));
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
                
                JPanel detailsPanel = new JPanel(new BorderLayout());
                detailsPanel.setBackground(ERROR_BG);
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
                
                JLabel detailsTitle = new JLabel("Stack Trace:");
                detailsTitle.setFont(new Font("Monospaced", Font.BOLD, 12));
                detailsTitle.setForeground(ERROR_TEXT);
                detailsPanel.add(detailsTitle, BorderLayout.NORTH);
                
                JPanel scrollPanel = new JPanel(new BorderLayout());
                scrollPanel.setBackground(ERROR_BG);
                scrollPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                scrollPanel.add(scrollPane, BorderLayout.CENTER);
                detailsPanel.add(scrollPanel, BorderLayout.CENTER);
                
                content.add(detailsPanel, BorderLayout.CENTER);
            }

            mainPanel.add(content, BorderLayout.CENTER);

            // Footer
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
            footer.setBackground(ERROR_BG);
            footer.setBorder(BorderFactory.createEmptyBorder(0, 30, 20, 30));

            JLabel footerLabel = new JLabel("JHR will automatically reload once you fix the error and save");
            footerLabel.setFont(new Font("Monospaced", Font.ITALIC, 11));
            footerLabel.setForeground(new Color(156, 163, 175));
            footer.add(footerLabel);

            mainPanel.add(footer, BorderLayout.SOUTH);

            frame.add(mainPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);

            // Add ESC key listener
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        frame.dispose();
                    }
                }
            });

            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });
    }

    public static void hide() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null && frame.isVisible()) {
                frame.dispose();
                frame = null;
            }
        });
    }

    public static void showCompilationError(String filePath, String errorOutput) {
        String title = "Compilation Failed";
        String message = "Failed to compile: " + filePath;
        show(title, message, errorOutput);
    }

    public static void showRuntimeError(String className, String errorOutput) {
        String title = "Runtime Error";
        String message = "Runtime error in: " + className;
        show(title, message, errorOutput);
    }
}
