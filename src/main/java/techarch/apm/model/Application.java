package techarch.apm.model;

import lombok.Getter;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Getter
@EqualsAndHashCode
public class Application {

    private String id, name, version, description;
    private Map<String, String> repositories;
    private Map<String, AppPackage> appPackages;
    private Map<String, Service> services;

    @Getter
    public static class AppPackage {
        private String repository, version;
    }

    @Getter
    public static class Service {
        private String version;
    }
}
