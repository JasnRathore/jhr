import javax.swing.JFrame;
import javax.swing.JLabel;

// Driver Class
public class Demo {
    // main function
    public static void main(String[] args)
    {
        // Create a new JFrame
        JFrame frame = new JFrame("My 600th JFrame");

        // Create a label
        JLabel label
            = new JLabel("porn  Premier League 2023");

        // Add the label to the frame
        frame.add(label);

        // Set frame properties
        frame.setSize(300,
                      200); // Set the size of the frame

        // Close operation
        frame.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE);

        // Make the frame visible
        frame.setVisible(true);
    }
}
