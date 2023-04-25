package techarch.apm.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import techarch.repository.Artifactory;

@Builder
@Getter
@EqualsAndHashCode
public class AppPackageIdentity implements Comparable<AppPackageIdentity> {

    private Artifactory artifactory;
    private String appPackageName;
    private String version;

    @Override
    public int compareTo(AppPackageIdentity o) {
        int compare = appPackageName.compareTo(o.appPackageName);
        if (compare != 0) return compare;
        compare = version.compareTo(o.version); // TODO: this should be changed to use semantic version comparison
        if (compare != 0) return compare;
        return artifactory.getRepositoryUrl().compareTo(o.artifactory.getRepositoryUrl());
    }
}
