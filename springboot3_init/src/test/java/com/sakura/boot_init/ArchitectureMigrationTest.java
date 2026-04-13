package com.sakura.boot_init;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 架构迁移约束测试，避免后续重新引入已移除的基础设施和旧包结构。
 */
class ArchitectureMigrationTest {

    /**
     * 校验生产代码中不再包含 Elasticsearch 和 MyBatis-Plus 引用。
     */
    @Test
    void shouldRemoveElasticsearchAndMybatisPlusReferences() throws IOException {
        Path sourceRoot = Path.of("src", "main");
        Path pomFile = Path.of("pom.xml");

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            List<Path> illegalFiles = Stream.concat(paths, Stream.of(pomFile))
                    .filter(Files::isRegularFile)
                    .filter(this::isTextSourceFile)
                    .filter(this::containsRemovedTechnology)
                    .toList();

            assertTrue(illegalFiles.isEmpty(), "仍存在 ES 或 MyBatis-Plus 引用：" + illegalFiles);
        }
    }

    /**
     * 校验顶层业务包只使用约定的业务域入口，防止继续出现平铺式目录。
     */
    @Test
    void shouldUseLayeredTopLevelPackages() throws IOException {
        Path javaRoot = Path.of("src", "main", "java", "com", "sakura", "boot_init");

        try (Stream<Path> paths = Files.list(javaRoot)) {
            List<Path> illegalPackages = paths
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        String packageName = path.getFileName().toString();
                        return !List.of("support", "user", "dict", "file", "wxmp", "agreement").contains(packageName);
                    })
                    .toList();

            assertTrue(illegalPackages.isEmpty(), "存在未归类的顶层包：" + illegalPackages);
        }
    }

    /**
     * 判断文件内容是否仍然引用被移除的技术栈。
     */
    private boolean containsRemovedTechnology(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return content.contains("com.baomidou")
                    || content.contains("mybatis-plus")
                    || content.contains("MyBatisPlus")
                    || content.contains("Elasticsearch")
                    || content.contains("elasticsearch");
        } catch (IOException e) {
            throw new IllegalStateException("读取源码文件失败：" + path, e);
        }
    }

    /**
     * 判断是否属于需要扫描的源码或配置文件。
     */
    private boolean isTextSourceFile(Path path) {
        String fileName = path.toString();
        return fileName.endsWith(".java")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".xml")
                || fileName.endsWith(".ftl");
    }
}
