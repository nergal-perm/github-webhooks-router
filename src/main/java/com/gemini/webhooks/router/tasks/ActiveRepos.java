package com.gemini.webhooks.router.tasks;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveRepos {
    private final Set<String> activeRepos = ConcurrentHashMap.newKeySet();

    public boolean contains(String repoName) {
        return this.activeRepos.contains(repoName);
    }

    public boolean lockRepoFor(AgentTask task) {
        return activeRepos.add(task.repoName());
    }

    public void unlockRepoFor(AgentTask task) {
        activeRepos.remove(task.repoName());
    }
}
