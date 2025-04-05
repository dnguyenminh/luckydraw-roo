package vn.com.fecredit.app.util;

import java.nio.file.*;
import java.io.*;
import java.util.regex.*;

/**
 * Utility class to help clean up code and fix common warnings.
 * Can be run as a standalone application to automatically fix common issues.
 */
public class CodeCleanupUtil {

    public static void main(String[] args) throws IOException {
        Path baseDir = Paths.get(".");
        removeUnusedImports(baseDir);
        addMissingNonNullAnnotations(baseDir);
    }
    
    private static void removeUnusedImports(Path baseDir) throws IOException {
        Files.walk(baseDir)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(CodeCleanupUtil::cleanImports);
    }
    
    private static void cleanImports(Path file) {
        try {
            String content = Files.readString(file);
            // Example: Remove lines with "import ... is never used" comments
            content = content.replaceAll("import [^;]+;\\s*// .*never used.*\\n", "");
            Files.writeString(file, content);
        } catch (IOException e) {
            System.err.println("Error processing " + file + ": " + e.getMessage());
        }
    }
    
    private static void addMissingNonNullAnnotations(Path baseDir) throws IOException {
        Files.walk(baseDir)
            .filter(p -> p.toString().endsWith(".java"))
            .forEach(CodeCleanupUtil::fixNonNullAnnotations);
    }
    
    private static void fixNonNullAnnotations(Path file) {
        try {
            String content = Files.readString(file);
            // Add import for NonNull if missing
            if (content.contains("Missing non-null annotation")) {
                if (!content.contains("import org.springframework.lang.NonNull;")) {
                    content = content.replaceFirst("package ([^;]+);", 
                        "package $1;\n\nimport org.springframework.lang.NonNull;");
                }
                // Look for method parameters missing @NonNull
                Pattern pattern = Pattern.compile("\\b(\\w+\\s+\\w+\\s*\\()([^@]\\w+)");
                Matcher matcher = pattern.matcher(content);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, matcher.group(1) + "@NonNull " + matcher.group(2));
                }
                matcher.appendTail(sb);
                content = sb.toString();
            }
            Files.writeString(file, content);
        } catch (IOException e) {
            System.err.println("Error processing " + file + ": " + e.getMessage());
        }
    }
}
