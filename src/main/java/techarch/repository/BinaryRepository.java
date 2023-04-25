package techarch.repository;

import java.io.IOException;
import java.io.InputStream;

public interface BinaryRepository {

    default InputStream getArtifact(final String group, final String artifactName, final String version, final String type) {
        return getArtifact(getArtifactPath(group, artifactName, version, type));
    }

    default InputStream getArtifact(final String artifactName, final String version, final String type) {
        return getArtifact(getArtifactPath(artifactName, version, type));
    }
    InputStream getArtifact(String path);
    void storeArtifact(InputStream artifactInputStream, String artifactName, String version, String type) throws IOException;

    default String getArtifactPath(final String group, final String artifactName, final String version, final String type) {
        return String.format("%s/%s", group.replaceAll("\\.", "/"), getArtifactPath(artifactName, version, type));
    }

    default String getArtifactPath(final String artifactName, final String version, final String type) {
        return String.format("%s/%s/%s-%s.%s", artifactName, version, artifactName, version, type);
    }
}
