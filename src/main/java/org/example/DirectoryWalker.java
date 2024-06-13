package org.example;

import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;


public class DirectoryWalker implements FileVisitor<Path> {


    private int fileCount;
    private int dirCount;
    private int aCount;
    private Set<String> extensions = new HashSet<>();
    private Map<String, Integer> extensionsCount = new HashMap<>();

    private static final Option TOTAL_NUM_FILES = new Option("a", "total-num-files", false, "Print the total number of files");
    private static final Option TOTAL_NUM_DIRS = new Option("b", "total-num-dirs", false, "Print the total number of directory");
    private static final Option TOTAL_NUM_UNIQUE = new Option("c", "total-unique-exts", false, "Print the total number of unique file extensions");
    private static final Option TOTAL_NUM_LISTS = new Option("d", "list-exts", false, "Print the list of all unique file extensions, i.e. only print each extension exacly once.");
    private static final Option TOTAL_NUM_EXT = new Option("", "num-ext", true, "Print the total number of files for the specified extension EXT (one extension at a time). This has no short option, only long option is supported.");
    private static final Option PATH_TO_FOLDER = new Option("f", true, "Specify the path to the documentation folder. This is a required argument.");

    public DirectoryWalker() {
    }


    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dirCount++;
        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (Files.isRegularFile(file)) {
            fileCount++;
            String fileExtensions = getFileExtensions(file);
            if (fileExtensions != null) {
                if (fileExtensions.equals("html")) {
                    String isA = new String(Files.readAllBytes(file));
                    for (int i = 0; i < isA.length(); i++) {
                        if (isA.charAt(i) == 'a') {
                            aCount++;
                        }
                    }
                }
                extensions.add(fileExtensions);
                int count = extensionsCount.getOrDefault(fileExtensions, 0);
                extensionsCount.put(fileExtensions, count + 1);
            }
        }

        return FileVisitResult.CONTINUE;
    }

    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("Failed");
        return FileVisitResult.TERMINATE;
    }

    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public int getFileCount() {
        return fileCount;
    }

    public int getDirCount() {
        return dirCount;
    }

    public int getExtensionsCount() {
        return extensions.size();
    }

    public Set<String> getExtensions() {
        return extensions;
    }

    public int getaCount() {
        return aCount;
    }

    public int getUniqueExtensionCount(String ext) {
        return extensionsCount.getOrDefault(ext, 0);
    }

    public String getFileExtensions(Path path) {
        String fileName = path.getFileName().toString();
        int index = fileName.lastIndexOf(".");
        if (index > 0 && index < fileName.length() - 1) {
            return fileName.substring(index + 1).toLowerCase();
        }
        return null;
    }


    public static void main(String[] args) {
        DirectoryWalker directoryWalker = new DirectoryWalker();
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(TOTAL_NUM_FILES);
        options.addOption(TOTAL_NUM_DIRS);
        options.addOption(TOTAL_NUM_UNIQUE);
        options.addOption(TOTAL_NUM_LISTS);
        options.addOption(TOTAL_NUM_EXT);
        options.addOption(PATH_TO_FOLDER);

        try {
            CommandLine commandLine = parser.parse(options, args);
            String folderPath = commandLine.getOptionValue(PATH_TO_FOLDER);
            if (folderPath != null) {
                Path startingPath = Paths.get(folderPath);
                Files.walkFileTree(startingPath, directoryWalker);
                if (commandLine.hasOption(TOTAL_NUM_FILES.getOpt()) || commandLine.hasOption(TOTAL_NUM_FILES.getLongOpt())) {
                    System.out.println(directoryWalker.getFileCount());
                }
                if (commandLine.hasOption(TOTAL_NUM_DIRS.getOpt()) || commandLine.hasOption(TOTAL_NUM_DIRS.getLongOpt())) {
                    System.out.println(directoryWalker.getDirCount());
                }
                if (commandLine.hasOption(TOTAL_NUM_UNIQUE.getOpt()) || commandLine.hasOption(TOTAL_NUM_UNIQUE.getLongOpt())) {
                    System.out.println(directoryWalker.getExtensionsCount());
                }

                if (commandLine.hasOption(TOTAL_NUM_LISTS.getOpt()) || commandLine.hasOption(TOTAL_NUM_LISTS.getLongOpt())) {
                    for (String extension : directoryWalker.getExtensions()) {
                        System.out.println(extension);
                    }
                }

                if (commandLine.hasOption(TOTAL_NUM_EXT.getOpt()) || commandLine.hasOption(TOTAL_NUM_EXT.getLongOpt())) {
                    System.out.println(directoryWalker.getUniqueExtensionCount(commandLine.getOptionValue(TOTAL_NUM_EXT)));
                }

                System.out.println(directoryWalker.getaCount());
            } else {
                System.out.println("Please provide the path to the documentation folder using the -f option.");
            }

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar file-walker.jar", options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
