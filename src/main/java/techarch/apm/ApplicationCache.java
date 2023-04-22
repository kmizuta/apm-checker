package techarch.apm;

import techarch.apm.model.AppPackage;
import techarch.apm.model.AppPackageIdentity;
import techarch.apm.model.Application;
import techarch.apm.repository.GitRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ApplicationCache {

    Map<String, GitRepository> getGitRespoitories();
    Set<techarch.apm.model.Application> getApplications();
    Map<AppPackageIdentity, AppPackage> getAppPackages();

    default GitRepository getGitRepository(final String remoteRepoUrl) {
        return getGitRespoitories().computeIfAbsent(remoteRepoUrl, (url) -> {
            return GitRepository.builder()
                    .remoteRepoUrl(url)
                    .build();
        });
    }

    default void addAll(List<Application> applicationJsonList) {
        getApplications().addAll(applicationJsonList);
    }

}
