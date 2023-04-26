package techarch.apm;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import techarch.apm.model.AppPackageIdentity;
import techarch.repository.Artifactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ApmChecker {
    public static void main(String[] args) {
        var config = args.length > 0 ?
                Config.builder().addSource(ConfigSources.file(Path.of(args[0])).build()).build() :
                Config.create();
        var repoUrlValue = config.get("application.repoUrl");
        var repoPathValue = config.get("application.repoPath");
        var repoUrl = repoUrlValue.asString().get();
        var repoPath = repoPathValue.exists() ? repoPathValue.asString().get() : null;
        var checker = new ApmChecker(repoUrl, repoPath);
        checker.run();
    }

    private final ApplicationCache cache;
    private final ApplicationLoader appLoader;
    private final AppPackageLoader appPkgLoader;
    private final PromotedAppPackages promotedAppPackages;

    public ApmChecker(final String repoUrl, final String repoPath) {
        this.cache = ApplicationCache.builder().build();
        var appLoaderBuilder = ApplicationLoader
                .builder()
                .applicationCache(cache)
                .remoteRepoUrl(repoUrl);
        if (repoPath != null)
            appLoaderBuilder.applicationJsonPath(repoPath);
        this.appLoader = appLoaderBuilder.build();
        this.appPkgLoader = AppPackageLoader
                .builder()
                .applicationCache(cache)
                .build();
        this.promotedAppPackages = PromotedAppPackages.getInstance();
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
        var appPkgVersionMap = new HashMap<String, List<String>>();
        appPkgs.keySet().stream().sorted().forEach( identity ->
                appPkgVersionMap.computeIfAbsent(
                        identity.getAppPackageName(),
                        k -> new ArrayList<>()).add(identity.getVersion()));
        appPkgVersionMap.forEach((pkg, versions) -> {
            Collections.sort(versions);
            System.out.print(pkg);
            var concat = ":";
            for (String version : versions) {
                System.out.print(concat);
                System.out.print(version);
                concat = ",";
            }
            System.out.println();
            if (versions.size() > 1) {
                System.out.println(String.format("\tMultiple versions. Will deploy %s.", versions.get(versions.size()-1)));
            }
            if (! promotedAppPackages.isPromoted(pkg, versions.get(versions.size()-1))) {
                System.out.println("\tVersion has not been promoted.");
            }
        });
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
