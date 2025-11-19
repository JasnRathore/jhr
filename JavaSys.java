import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JavaSys {

  private Process runningProcess;
  private Thread outputThread;
  private final Config config;
  private boolean hasError = false;

  public JavaSys(Config config) {
    this.config = config;
  }

  boolean Compile(String path) {
    try {
      List<String> command = new ArrayList<>();
      command.add("javac");
command.add("-cp");
      command.add(config.classpath);
      command.add("-d");
      command.add("temp");
      
      
      // Add compiler flags if specified
      if (!config.javacFlags.isEmpty()) {
        for (String flag : config.javacFlags.split(" ")) {
          if (!flag.isEmpty()) {
            command.add(flag);
          }
        }
      }
      
      command.add(path);

      ProcessBuilder processBuilder = new ProcessBuilder(command);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      StringBuilder output = new StringBuilder();

      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }

      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println("✓ Compiled: " + path);
        // Clear error overlay if compilation succeeds
        if (hasError) {
          ErrorOverlay.hide();
          hasError = false;
        }
        return true;
      } else {
        System.err.println("✗ Compilation failed: " + path);
        System.err.println("Exit Code: " + exitCode);
        String errorOutput = output.toString().trim();
        if (!errorOutput.isEmpty()) {
          System.err.println(errorOutput);
        }
        
        // Show error overlay
        hasError = true;
        ErrorOverlay.showCompilationError(path, errorOutput);
        return false;
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      hasError = true;
      ErrorOverlay.showCompilationError(path, e.toString());
      return false;
    }
  }

  void Run(String className, String classpath, String[] args) {
    try {
      // Kill old process if still running
      if (runningProcess != null && runningProcess.isAlive()) {
        System.out.println("⏹  Stopping previous process...");
        runningProcess.destroyForcibly();
        runningProcess.waitFor();
      }

      // Wait for output thread to finish
      if (outputThread != null && outputThread.isAlive()) {
        outputThread.interrupt();
        outputThread.join(1000);
      }

      // d command
      List<String> command = new ArrayList<>();
      command.add("java");
      
      // Add JVM arguments if specified
      if (!config.jvmArgs.isEmpty()) {
        for (String arg : config.jvmArgs.split(" ")) {
          if (!arg.isEmpty()) {
            command.add(arg);
          }
        }
      }
      command.add("-cp");
        String fullClasspath = classpath 
                + System.getProperty("path.separator") 
                + "temp";
        command.add(fullClasspath);
      command.add(className);
      
      // Add application arguments
      for (String arg : args) {
        if (!arg.isEmpty()) {
          command.add(arg);
        }
      }

      ProcessBuilder processBuilder = new ProcessBuilder(command);
      runningProcess = processBuilder.start();

      // Stream output asynchronously
      outputThread = new Thread(() -> {
        try {
          BufferedReader stdOut = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()));
          BufferedReader stdErr = new BufferedReader(new InputStreamReader(runningProcess.getErrorStream()));

          StringBuilder errorOutput = new StringBuilder();
          boolean hasRuntimeError = false;

          // Read stderr in separate thread
          Thread stderrThread = new Thread(() -> {
            try {
              String errLine;
              while ((errLine = stdErr.readLine()) != null) {
                System.err.println(errLine);
                errorOutput.append(errLine).append("\n");
                
                // Detect runtime errors
                if (errLine.contains("Exception") || errLine.contains("Error")) {
                  synchronized (errorOutput) {
                    errorOutput.notify();
                  }
                }
              }
            } catch (IOException ignored) {
            }
          });
          stderrThread.setDaemon(true);
          stderrThread.start();

          // Read stdout
          String line;
          while ((line = stdOut.readLine()) != null) {
            System.out.println(line);
          }

          int exitCode = runningProcess.waitFor();
          
          // Wait a bit for stderr to finish
          stderrThread.join(500);
          
          String errorStr = errorOutput.toString().trim();
          
          if (exitCode != 0 && !errorStr.isEmpty()) {
            System.err.println("\n⚠  Process exited with code: " + exitCode);
            hasError = true;
            ErrorOverlay.showRuntimeError(className, errorStr);
          } else if (exitCode == 0 && hasError) {
            // Process completed successfully, hide error if any
            ErrorOverlay.hide();
            hasError = false;
          }

        } catch (IOException | InterruptedException ignored) {
        }
      });
      outputThread.setDaemon(true);
      outputThread.start();

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      hasError = true;
      ErrorOverlay.showRuntimeError(className, e.toString());
    }
  }
}
