package techarch.apm;


import com.fasterxml.jackson.databind.ObjectMapper;
import techarch.apm.model.Application;

import java.io.IOException;
import java.util.List;

public class ApplicationLoader {

    private final InMemoryApplicationCache appCache;

    private final String remoteRepoUrl;
    private final String applicationJsonPath;

    private ApplicationLoader(final InMemoryApplicationCache applicationCache, final String remoteRepoUrl, final String applicationJsonPath) {
        this.appCache = applicationCache;
        this.remoteRepoUrl = remoteRepoUrl;
        this.applicationJsonPath = applicationJsonPath;
    }

    public static Builder builder() {
        return new Builder();
    }


    public List<Application> getApplications() {
        try (var gitRepo = appCache.getGitRepository(remoteRepoUrl)) {

            var inputStream = gitRepo.getFile(applicationJsonPath);
            var mapper = new ObjectMapper();
            var javaType = mapper.getTypeFactory().constructCollectionType(List.class, techarch.apm.model.Application.class);
            List<Application> applicationJsonList = mapper.readValue(inputStream, javaType);
            appCache.addAll(applicationJsonList);
            return applicationJsonList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static class Builder {

        private String remoteRepoUrl;
        private String applicationJsonPath = "intg/helm/etc/config/application.json";
        private InMemoryApplicationCache applicationCache;

        public Builder remoteRepoUrl(final String remoteRepoUrl) {
            this.remoteRepoUrl = remoteRepoUrl;
            return this;
        }

        public Builder applicationJsonPath(final String applicationJsonPath) {
            this.applicationJsonPath = applicationJsonPath;
            return this;
        }

        public Builder applicationCache(final InMemoryApplicationCache applicationCache) {
            this.applicationCache = applicationCache;
            return this;
        }

        public ApplicationLoader build() {
            return new ApplicationLoader(applicationCache, remoteRepoUrl, applicationJsonPath);
        }

    }
}
