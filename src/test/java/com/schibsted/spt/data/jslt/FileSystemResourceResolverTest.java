package com.schibsted.spt.data.jslt;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.impl.FileSystemResourceResolver;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileSystemResourceResolverTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public final void testResolveImportsFromFilesystem() throws IOException  {
        final FileSystemResourceResolver resolver = new FileSystemResourceResolver();
        final Path jslt = FileSystems.getDefault().getPath("./src/test/resources/import-from-fs/working1.jslt");
        final Expression e = new Parser(
            new InputStreamReader(new FileInputStream(jslt.toFile()), StandardCharsets.UTF_8), resolver)
                .compile();
        assertEquals(
                readResource("import-from-fs/working1_expected_result.json"),
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(e.apply(mapper.readTree("{}"))));
    }

    private final String readResource(final String path) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader()
                    .getResource(path).toURI())), StandardCharsets.UTF_8);
        } catch (final URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
