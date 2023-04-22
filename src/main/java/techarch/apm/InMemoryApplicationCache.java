package techarch.apm;

import lombok.Getter;
import techarch.apm.model.AppPackage;
import techarch.apm.model.AppPackageIdentity;
import techarch.apm.model.Application;
import techarch.apm.repository.GitRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class InMemoryApplicationCache implements ApplicationCache {
    private final Map<String, GitRepository> gitRespoitories = new HashMap<>();
    private final Set<Application> applications = new HashSet<>();
    private final Map<AppPackageIdentity, AppPackage> appPackages = new HashMap<>();

    private InMemoryApplicationCache() { }

    public static Builder builder() { return new Builder(); }


    public static class Builder {
        public InMemoryApplicationCache build() {
            return new InMemoryApplicationCache();
        }
    }
}
