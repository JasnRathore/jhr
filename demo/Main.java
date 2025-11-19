import javax.swing.JFrame;
import javax.swing.JLabel;
import test2.IAmTest;

public class Main {
    // main function
    public static void main(String[] args)
    {
        // Create a new JFrame
        JFrame frame = new JFrame("My 600th JFrame");

        Test to = new Test();
        IAmTest t2 = new IAmTest();
        // Create a label
        JLabel label
            = new JLabel(to.getContent() + t2.getContent());

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
