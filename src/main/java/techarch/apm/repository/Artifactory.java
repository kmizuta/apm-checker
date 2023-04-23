package techarch.apm.repository;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;

import java.io.InputStream;


@EqualsAndHashCode
@Slf4j
public class Artifactory implements BinaryRepository {
    public static final String PROXY_SERVER = "http://www-proxy-hqdc.us.oracle.com:80";

    private final String repositoryUrl;
    private final boolean usesProxy;

    public Artifactory(final String repositoryUrl, final boolean usesProxy) {
        this.repositoryUrl = repositoryUrl;
        this.usesProxy = usesProxy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public InputStream getArtifact(String path) {
        var clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        if (usesProxy) clientConfig.property(ClientProperties.PROXY_URI, PROXY_SERVER);
        var client = JerseyClientBuilder.newBuilder().withConfig(clientConfig).build();
        var artifactoryTarget = client.target(repositoryUrl);
        var artifactTarget = artifactoryTarget.path(path);
        var response = artifactTarget.request().get();

        if (log.isDebugEnabled()) {
            log.debug("Artifact URL = {}", artifactTarget.getUri().toString());
            log.debug("Status = {}", response.getStatus());
            log.debug("Content-Length = {}", response.getHeaderString("content-length"));
        }
        return response.readEntity(InputStream.class);
    }

    public InputStream getArtifact(final String artifactName, final String version, final String type) {
        return getArtifact(String.format("%s/%s/%s-%s.%s", artifactName, version, artifactName, version, type));
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void storeArtifact(InputStream artifactInputStream, String artifactName, String version, String type) {
        throw new UnsupportedOperationException();
    }

    public static class Builder {

        private String repositoryUrl = null;
        private boolean usesProxy = false;

        public Builder repositoryUrl(final String repositoryUrl) {
            this.repositoryUrl = repositoryUrl;
            return this;
        }

        public Builder useProxy() {
            usesProxy = true;
            return this;
        }

        public Artifactory build() {
            return new Artifactory(repositoryUrl, usesProxy);
        }

    }
}
