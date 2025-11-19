import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JavaSys {

  private Process runningProcess;

  void Compile(String path) {
    try {
      ProcessBuilder processBuilder = new ProcessBuilder("javac", path);
      Process process = processBuilder.start();

      BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

      String line;
      StringBuilder output = new StringBuilder();

      while ((line = stdOut.readLine()) != null) {
        output.append(line).append("\n");
      }
      while ((line = stdErr.readLine()) != null) {
        output.append(line).append("\n");
      }

      int exitCode = process.waitFor();

      System.out.println("Compiling: " + path);
      System.out.println("Exit Code: " + exitCode);
      System.out.println("\n\n" + output.toString());

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  void Run(String className, String classpath, String[] args) {
    try {
      // Kill old process if still running
      if (runningProcess != null && runningProcess.isAlive()) {
        System.out.println("Killing previous running process.");
        runningProcess.destroy();
        runningProcess.waitFor(); // Optionally wait for it to terminate
      }

      // Allocate array with 4 + args.length for the command
      String[] command = new String[4 + args.length];
      command[0] = "java";
      command[1] = "-cp";
      command[2] = classpath;
      command[3] = className;
      System.arraycopy(args, 0, command, 4, args.length);

      ProcessBuilder processBuilder = new ProcessBuilder(command);
      runningProcess = processBuilder.start();

      BufferedReader stdOut = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()));
      BufferedReader stdErr = new BufferedReader(new InputStreamReader(runningProcess.getErrorStream()));

      String line;
      StringBuilder output = new StringBuilder();

      while ((line = stdOut.readLine()) != null) {
        output.append(line).append("\n");
      }
      while ((line = stdErr.readLine()) != null) {
        output.append(line).append("\n");
      }

      int exitCode = runningProcess.waitFor();

      System.out.println("Running: " + String.join(" ", command));
      System.out.println("Exit Code: " + exitCode);
      System.out.println("\n\n" + output.toString());

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
