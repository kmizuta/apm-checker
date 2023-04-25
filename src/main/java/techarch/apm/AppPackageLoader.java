package techarch.apm;

import com.fasterxml.jackson.databind.ObjectMapper;
import techarch.apm.model.AppPackage;
import techarch.apm.model.AppPackageIdentity;
import techarch.repository.Artifactory;
import techarch.repository.BinaryRepository;
import techarch.repository.LocalBinaryRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AppPackageLoader {
    private static final String APP_PACKAGE_EXTENSION = "apg";
    private static final String APP_PACKAGE_JSON_PATH = "build/app-package.json";
    private static final LocalBinaryRepository LOCAL_REPOSITORY = LocalBinaryRepository.builder().build();
    private final ApplicationCache applicationCache;

    private AppPackageLoader(ApplicationCache applicationCache) {
        this.applicationCache = applicationCache;
    }

    public static Builder builder() {
        return new Builder();
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



    public AppPackage getAppPackage(final Artifactory artifactory, final String appPackageName, final String version) {
        return getAppPackage(AppPackageIdentity.builder()
                .artifactory(artifactory)
                .appPackageName(appPackageName)
                .version(version).build());
    }

    public AppPackage getAppPackage(final AppPackageIdentity appPackageIdentity) {
        return applicationCache.getAppPackages().computeIfAbsent(appPackageIdentity, (identity) ->
                fetchAppPackage(appPackageIdentity));
    }

    private AppPackage fetchAppPackage(final AppPackageIdentity appPackageIdentity) {
        var apgInputStream = fetchAppPackageInputStream(LOCAL_REPOSITORY, appPackageIdentity);
        if (apgInputStream == null) {
            apgInputStream = fetchAppPackageInputStream(appPackageIdentity.getArtifactory(), appPackageIdentity);
            try {
                LOCAL_REPOSITORY.storeArtifact(apgInputStream, appPackageIdentity.getAppPackageName(), appPackageIdentity.getVersion(), APP_PACKAGE_EXTENSION);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            apgInputStream = fetchAppPackageInputStream(LOCAL_REPOSITORY, appPackageIdentity);
        }

        return extractAppPackage(apgInputStream);

    }

    private InputStream fetchAppPackageInputStream(final BinaryRepository repository, AppPackageIdentity appPackageIdentity) {
        var appPackageName = appPackageIdentity.getAppPackageName();
        var version = appPackageIdentity.getVersion();

        return repository.getArtifact(appPackageName, version, APP_PACKAGE_EXTENSION);
    }

    private AppPackage extractAppPackage(final InputStream inputStream) {
        try {
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
        private ApplicationCache applicationCache;

        public Builder applicationCache(ApplicationCache applicationCache) {
            this.applicationCache = applicationCache;
            return this;
        }

        public AppPackageLoader build() {
            return new AppPackageLoader(applicationCache);
        }
    }
}
