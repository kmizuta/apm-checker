package techarch.apm;

import com.fasterxml.jackson.databind.ObjectMapper;
import techarch.apm.model.Application;
import techarch.apm.model.AppPackage;
import techarch.apm.model.AppPackageIdentity;
import techarch.apm.repository.Artifactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppPackageLoader {
    private static final String APP_PACKAGE_EXTENSION = "apg";
    private static final String APP_PACKAGE_JSON_PATH = "build/app-package.json";

    private final InMemoryApplicationCache applicationCache;

    private AppPackageLoader(InMemoryApplicationCache applicationCache) {
        this.applicationCache = applicationCache;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<AppPackage> getAppPackages(final Application application) {
        final List<AppPackage> appPackageList = new ArrayList<>();
        application.getAppPackages().forEach( (name, appPackage) -> {
            var repositoryUrl = application.getRepositories().get(appPackage.getRepository());
            var artifactory = Artifactory.builder().repositoryUrl(repositoryUrl).useProxy().build();
            var version = VersionUtil.toExactVersion(appPackage.getVersion());

            appPackageList.add(getAppPackage(artifactory, name, version));
        });

        return appPackageList;
    }

    public List<AppPackage> getAppPackageDependencies(final AppPackageIdentity appPackageIdentity) {
        var appPackage = getAppPackage(appPackageIdentity);
        if (appPackage == null) throw new IllegalArgumentException();

        final List<AppPackage> dependentAppPackages = new ArrayList<>();

        var dependencies = appPackage.getDependencies();
        if (dependencies == null) return dependentAppPackages;

        dependencies.forEach( (name, version)
                -> dependentAppPackages.add(getAppPackage(appPackageIdentity.getArtifactory(), name, VersionUtil.toExactVersion(version))));

        return dependentAppPackages;
    }



    public techarch.apm.model.AppPackage getAppPackage(final Artifactory artifactory, final String appPackageName, final String version) {
        return getAppPackage(AppPackageIdentity.builder()
                .artifactory(artifactory)
                .appPackageName(appPackageName)
                .version(version).build());
    }

    public techarch.apm.model.AppPackage getAppPackage(final AppPackageIdentity appPackageIdentity) {
        return applicationCache.getAppPackages().computeIfAbsent(appPackageIdentity, (identity) -> {
            return fetchAppPackage(appPackageIdentity);
        });
    }

    private techarch.apm.model.AppPackage fetchAppPackage(final AppPackageIdentity appPackageIdentity) {
        var artifactory = appPackageIdentity.getArtifactory();
        var appPackageName = appPackageIdentity.getAppPackageName();
        var version = appPackageIdentity.getVersion();

        try {
            var inputStream = artifactory.getArtifact(appPackageName, version, APP_PACKAGE_EXTENSION);
            var zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (APP_PACKAGE_JSON_PATH.equals(zipEntry.getName())) {
                    var appPackageContents = getContent(zipInputStream);
                    var mapper = new ObjectMapper();
                    return mapper.readValue(appPackageContents, AppPackage.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private String getContent(ZipInputStream zipInputStream) {
        var baos = new ByteArrayOutputStream();
        var bytes = new byte[1024];
        int len;
        try {
            while ((len = zipInputStream.read(bytes)) != -1) {
                baos.write(bytes, 0, len);
            }
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toString();
    }


    public static class Builder {
        private InMemoryApplicationCache applicationCache;

        public Builder applicationCache(InMemoryApplicationCache applicationCache) {
            this.applicationCache = applicationCache;
            return this;
        }

        public AppPackageLoader build() {
            return new AppPackageLoader(applicationCache);
        }
    }
}
