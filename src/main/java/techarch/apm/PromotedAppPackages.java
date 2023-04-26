package techarch.apm;

import com.opencsv.CSVReader;

import lombok.Builder;
import lombok.Getter;
import techarch.repository.GitRepository;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PromotedAppPackages {
/*
"V3 Org Name","V3 Service Name","App-package Name","App-package Version","Release","NGFABS Build"
"common","apps-infra","ora_common_appsInfra_objects","2307.0.44","23.07","https://ngfabs.oraclecorp.com/fabs/buildResults?snapshotName=RWD-INFRASTRUCTURE-BUSINESSOBJECTS_230424.125858252"
"common","setup","ora_common_setup_features","2307.0.32","23.07","https://ngfabs.oraclecorp.com/fabs/buildResults?snapshotName=SETUP-FSM_FSM-BOSS_230411.000524223"
 */

    private static PromotedAppPackages instance = new PromotedAppPackages();
    public static PromotedAppPackages getInstance() {
        return instance;
    }

    private final List<AppPackage> appPackages;

    private PromotedAppPackages() {
        this.appPackages = new ArrayList<>();

        var gitRepo = GitRepository.builder()
                .remoteRepoUrl("ssh://kenichi.mizuta%40oracle.com@alm.oraclecorp.com/fusionapps_fa-service-metadata_35259/fa-apm-promotion.git")
                .build();

        var csvFile = gitRepo.getFile("fa-promoted-app-package.csv");
        try {
            var csvReader = new CSVReader(new FileReader(csvFile));
            var record = csvReader.readNext(); // Skip title row
            while ((record = csvReader.readNext()) != null) {
                int i=0;
                appPackages.add(AppPackage.builder()
                        .org(record[i++])
                        .service(record[i++])
                        .name(record[i++])
                        .version(record[i++])
                        .release(record[i++])
                        .ngfabsBuildUrl(record[i++])
                        .build());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPromoted(final String appPackageName, final String version) {
        for ( var p : appPackages ) {
            if (p.name.equals(appPackageName) && p.version.equals(version))
                return true;
        }
        return false;
    }

    public List<String> findPromotedVersions(final String appPackageName) {
        final var retval = new ArrayList<String>();
        appPackages.forEach( p -> {
            if (p.name.equals(appPackageName))
                retval.add(p.version);
        });
        return retval;
    }

    @Builder
    public static class AppPackage {
        private String org, service, name, version, release, ngfabsBuildUrl;
    }
}
