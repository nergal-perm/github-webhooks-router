package com.gemini.webhooks.router.storage;

import java.io.IOException;
import java.nio.file.Path;

public interface TaskRepository {
    Path save(String filename, String content) throws IOException;
}
