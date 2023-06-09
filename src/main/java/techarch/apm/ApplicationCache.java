package techarch.apm;

import lombok.Getter;
import techarch.apm.model.AppPackage;
import techarch.apm.model.AppPackageIdentity;
import techarch.apm.model.Application;
import techarch.repository.GitRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class ApplicationCache {
    private final Map<String, GitRepository> gitRespoitories = new HashMap<>();
    private final Set<Application> applications = new HashSet<>();
    private final Map<AppPackageIdentity, AppPackage> appPackages = new HashMap<>();

    private ApplicationCache() { }

    public static Builder builder() { return new Builder(); }

    public GitRepository getGitRepository(final String remoteRepoUrl) {
        return getGitRespoitories().computeIfAbsent(remoteRepoUrl, (url) -> GitRepository.builder()
                .remoteRepoUrl(url)
                .build());
    }

    public void addAll(List<Application> applicationJsonList) {
        getApplications().addAll(applicationJsonList);
    }


    public static class Builder {
        public ApplicationCache build() {
            return new ApplicationCache();
        }
    }
}
