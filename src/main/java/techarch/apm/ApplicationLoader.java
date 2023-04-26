package techarch.apm;


import com.fasterxml.jackson.databind.ObjectMapper;
import techarch.apm.model.Application;

import java.io.IOException;
import java.util.List;

public class ApplicationLoader {

    private final List<Application> applicationJsonList;

    private ApplicationLoader(final ApplicationCache applicationCache, final String remoteRepoUrl, final String applicationJsonPath) {
        try (var gitRepo = applicationCache.getGitRepository(remoteRepoUrl)) {

            var inputStream = gitRepo.getFile(applicationJsonPath);
            var mapper = new ObjectMapper();
            var javaType = mapper.getTypeFactory().constructCollectionType(List.class, techarch.apm.model.Application.class);
            this.applicationJsonList = mapper.readValue(inputStream, javaType);
            applicationCache.addAll(applicationJsonList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ApplicationLoader(final ApplicationCache applicationCache, final String applicationJson) {
        try {
            var mapper = new ObjectMapper();
            var javaType = mapper.getTypeFactory().constructCollectionType(List.class, techarch.apm.model.Application.class);
            this.applicationJsonList = mapper.readValue(applicationJson, javaType);
            applicationCache.addAll(applicationJsonList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }


    public List<Application> getApplications() {
        return applicationJsonList;
    }


    public static class Builder {

        private String remoteRepoUrl;
        private String applicationJsonPath = "intg/helm/etc/config/application.json";
        private ApplicationCache applicationCache;
        private String applicationJson;

        public Builder remoteRepoUrl(final String remoteRepoUrl) {
            this.remoteRepoUrl = remoteRepoUrl;
            return this;
        }

        public Builder applicationJsonPath(final String applicationJsonPath) {
            this.applicationJsonPath = applicationJsonPath;
            return this;
        }

        public Builder applicationCache(final ApplicationCache applicationCache) {
            this.applicationCache = applicationCache;
            return this;
        }

        public Builder applicationJson(final String applicationJson) {
            this.applicationJson = applicationJson;
            return this;
        }

        public ApplicationLoader build() {
            if (applicationJson != null) {
                return new ApplicationLoader(applicationCache, applicationJson);
            } else {
                return new ApplicationLoader(applicationCache, remoteRepoUrl, applicationJsonPath);
            }
        }

    }
}
