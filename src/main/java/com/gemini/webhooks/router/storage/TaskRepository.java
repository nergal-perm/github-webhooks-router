package com.gemini.webhooks.router.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TaskRepository {
    Path save(String filename, String content) throws IOException;

    List<String> list() throws IOException;

    List<String> list(Path directory) throws IOException;

    Path move(String filename, Path fromDir, Path toDir) throws IOException;
}
