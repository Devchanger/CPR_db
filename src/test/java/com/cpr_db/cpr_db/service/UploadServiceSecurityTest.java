package com.cpr_db.cpr_db.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class UploadServiceSecurityTest {

    private UploadService uploadService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService();
        ReflectionTestUtils.setField(uploadService, "uploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("P0-5 parent traversal ../../evil.png must not escape upload dir")
    void uploadImage_withParentTraversal_doesNotEscape() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../evil.png", "image/png", "fake-bytes".getBytes());
        uploadService.uploadImage(file);
        assertNoEscape("evil.png");
    }

    @Test
    @DisplayName("P0-5 url encoded traversal ..%2f..%2fevil.png must not escape")
    void uploadImage_withUrlEncodedTraversal_doesNotEscape() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "..%2f..%2fevil.png", "image/png", "fake-bytes".getBytes());
        uploadService.uploadImage(file);
        assertNoEscape("evil.png");
    }

    @Test
    @DisplayName("P0-5 windows-style separator traversal must be sanitized")
    void uploadImage_withBackslashTraversal_doesNotEscape() throws Exception {
        String name = new StringBuilder("..")
                .append(java.io.File.separator).append("..")
                .append(java.io.File.separator).append("evil.png")
                .toString();
        MockMultipartFile file = new MockMultipartFile(
                "file", name, "image/png", "fake-bytes".getBytes());
        uploadService.uploadImage(file);
        assertNoEscape("evil.png");
    }

    @Test
    @DisplayName("P0-5 getExtension strips traversal from malicious names")
    void getExtension_sanitizesMaliciousNames() throws Exception {
        String[] malicious = {
                "../../evil.png",
                "..%2f..%2fevil.png",
                "/tmp/x.png",
                "a/../../b.png",
                "normal.jpg"
        };
        for (String name : malicious) {
            String ext = invokeGetExtension(name);
            assertFalse(ext.contains(".."), "ext must not contain .. : " + name + " -> " + ext);
            assertFalse(ext.contains("/"), "ext must not contain / : " + name + " -> " + ext);
            assertFalse(ext.indexOf(java.io.File.separatorChar) >= 0,
                    "ext must not contain separator : " + name + " -> " + ext);
        }
        assertEquals("jpg", invokeGetExtension("normal.jpg"));
        assertEquals("png", invokeGetExtension("../../evil.png"));
    }

    @Test
    @DisplayName("P0-5 generateStoredName is always a UUID with no separators")
    void generateStoredName_isUuidWithoutSeparators() throws Exception {
        String withExt = invokeGenerateStoredName("png");
        String noExt = invokeGenerateStoredName("");
        Pattern uuid = Pattern.compile("^[0-9a-fA-F-]{36}([.][a-z0-9]+)?$");
        assertTrue(uuid.matcher(withExt).matches(), "stored name should be UUID: " + withExt);
        assertTrue(uuid.matcher(noExt).matches(), "stored name should be UUID: " + noExt);
        assertFalse(withExt.contains("/") || withExt.contains(".."), "stored name must not contain path separators");
    }

    private void assertNoEscape(String forbiddenBaseName) throws Exception {
        List<Path> written = Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .toList();
        assertFalse(written.isEmpty(), "at least one file should be written");
        for (Path pp : written) {
            String name = pp.getFileName().toString();
            assertFalse(name.contains(".."), "stored file name must not contain .. : " + name);
            assertFalse(name.contains("/"), "stored file name must not contain / : " + name);
            assertFalse(name.equals(forbiddenBaseName), forbiddenBaseName + " must not be created");
            assertTrue(pp.toAbsolutePath().startsWith(tempDir.toAbsolutePath()),
                    "file escaped upload dir: " + pp);
        }
        long uuidPng = written.stream()
                .filter(pp -> pp.getFileName().toString().matches("[0-9a-fA-F-]{36}[.]png"))
                .count();
        assertEquals(1, uuidPng, "exactly one UUID.png file should be written");
    }

    private String invokeGetExtension(String name) throws Exception {
        Method m = UploadService.class.getDeclaredMethod("getExtension", String.class);
        m.setAccessible(true);
        return (String) m.invoke(uploadService, name);
    }

    private String invokeGenerateStoredName(String ext) throws Exception {
        Method m = UploadService.class.getDeclaredMethod("generateStoredName", String.class);
        m.setAccessible(true);
        return (String) m.invoke(uploadService, ext);
    }
}
