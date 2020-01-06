package com.schibsted.spt.data.jslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.impl.FileSystemResourceResolver;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class FileSystemResourceResolverTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testResolveImportsFromFilesystem() throws IOException  {
        FileSystemResourceResolver resolver = new FileSystemResourceResolver();
        Path jslt = FileSystems.getDefault().getPath("./src/test/resources/import-from-fs/working1.jslt");
        Expression e = new Parser(
          new InputStreamReader(new FileInputStream(jslt.toFile()))
        )
          .withResourceResolver(resolver)
          .compile();
        assertEquals(
                readResource("import-from-fs/working1_expected_result.json"),
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.apply(mapper.readTree("{}"))));
    }

    @Test
    public void testResolveImportsFromFilesystemWithExplicitRootPath() throws IOException  {
        FileSystemResourceResolver resolver =
          new FileSystemResourceResolver(FileSystems.getDefault().getPath("src/test/resources/import-from-fs"));
        Path jslt = FileSystems.getDefault().getPath("./src/test/resources/import-from-fs/working2.jslt");
        Expression e = new Parser(
          new InputStreamReader(new FileInputStream(jslt.toFile()))
        )
          .withResourceResolver(resolver)
          .compile();
        assertEquals(
                readResource("import-from-fs/working1_expected_result.json"),
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.apply(mapper.readTree("{}"))));
    }

    @Test
    public void testResolveImportsFromFilesystemNotWorking() throws IOException  {
        FileSystemResourceResolver resolver = new FileSystemResourceResolver();
        Path jslt = FileSystems.getDefault().getPath("./src/test/resources/import-from-fs/wrong_relative_path.jslt");
        try {
            new Parser(
              new InputStreamReader(new FileInputStream(jslt.toFile()))
            )
              .withResourceResolver(resolver)
              .compile();
            fail("Expected " + JsltException.class.getSimpleName());
        } catch (final JsltException e) {
            assertEquals(NoSuchFileException.class, e.getCause().getClass());
        }
    }

    private String readResource(final String path) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                    .getResource(path).toURI())), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
