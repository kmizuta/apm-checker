package techarch.apm;

import io.helidon.config.Config;
import techarch.apm.model.AppPackageIdentity;
import techarch.apm.repository.Artifactory;

public class ApmChecker {
    public static void main(String[] args) {
        var config = Config.create();
        var repoUrlValue = config.get("application.repoUrl");
        var repoUrl = repoUrlValue.asString().get();
        var checker = new ApmChecker(repoUrl);
        checker.run();
    }

    private final ApplicationCache cache;
    private final ApplicationLoader appLoader;
    private final AppPackageLoader appPkgLoader;

    public ApmChecker(final String remoteRepoUrl) {
        this.cache = ApplicationCache.builder().build();
        this.appLoader = ApplicationLoader
                .builder()
                .applicationCache(cache)
                .remoteRepoUrl(remoteRepoUrl)
                .build();
        this.appPkgLoader = AppPackageLoader
                .builder()
                .applicationCache(cache)
                .build();
    }

    public void run() {

        System.out.println("==============================================");
        System.out.println("Dependency Tree");
        System.out.println("==============================================");
        var applications = appLoader.getApplications();
        applications.forEach( (app) -> app.getAppPackages().forEach( (appPkgName, appAppPkg) -> {
            var artifactory = Artifactory
                    .builder()
                    .repositoryUrl(app.getRepositories().get(appAppPkg.getRepository()))
                    .useProxy()
                    .build();
            var identity = AppPackageIdentity
                    .builder()
                    .artifactory(artifactory)
                    .appPackageName(appPkgName)
                    .version(VersionUtil.toExactVersion(appAppPkg.getVersion()))
                    .build();
            traverseDependencies(0, identity);
        }));

        System.out.println(); System.out.println();
        System.out.println("==============================================");
        System.out.println("List of App Packages");
        System.out.println("==============================================");
        var appPkgs = cache.getAppPackages();
        appPkgs.keySet().stream().sorted().forEach( identity ->
                System.out.println(String.format("%s:%s", identity.getAppPackageName(), identity.getVersion())));
    }

    private void traverseDependencies(int level, AppPackageIdentity identity) {
        var appPackage = appPkgLoader.getAppPackage(identity);
        if (appPackage == null) {
            System.err.println(String.format("Unable to locate app package - %s:%s from %s",
                    identity.getAppPackageName(), identity.getVersion(), identity.getArtifactory().getRepositoryUrl()));
            return;
        }
        for (int i=0; i<level; i++) System.out.print("   ");
        System.out.println(String.format("%s:%s - %s", appPackage.getName(), appPackage.getVersion(), appPackage.getDescription()));

        var dependencies = appPkgLoader.getAppPackageDependencies(identity);
        if (dependencies == null) return;

        dependencies.forEach( depAppPackage ->
                traverseDependencies(level+1, AppPackageIdentity.builder()
                        .artifactory(identity.getArtifactory())
                        .appPackageName(depAppPackage.getName())
                        .version(VersionUtil.toExactVersion(depAppPackage.getVersion()))
                        .build()
                ));
    }

}
