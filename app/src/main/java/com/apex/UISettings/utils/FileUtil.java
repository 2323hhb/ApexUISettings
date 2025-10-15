package com.apex.UISettings.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtil {

    // Private constructor to prevent instantiation
    private FileUtil() {
    }

    public static String readFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String readFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean writeToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean checkFileExists(String filePath) {
        return new File(filePath).exists();
    }

    public static boolean createFile(String filePath) {
        if (checkFileExists(filePath)) {
            return true;
        }
        try {
            return new File(filePath).createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createDir(String filePath) {
        if (checkFileExists(filePath)) {
            return true;
        }
        try {
            return new File(filePath).mkdir();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean writeToFile(String filePath, byte[] content) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(content);
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void copyFile(String sourcePath, String destinationPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destinationFile = new File(destinationPath);
        if (!sourceFile.exists()) {
            return;
        }
        createParentDirectory(destinationFile.toPath());
        // 使用 Files 类的 copy() 方法进行文件复制
        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // 创建父目录（包含多级目录）
    public static void createParentDirectory(Path path) throws IOException {
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
    }
}
