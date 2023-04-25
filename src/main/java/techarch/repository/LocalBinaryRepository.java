package techarch.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocalBinaryRepository implements BinaryRepository {

    private final File repositoryFile;

    private LocalBinaryRepository(final File repositoryFile) {
        this.repositoryFile = repositoryFile;
    }

    public static Builder builder() { return new Builder(); }

    @Override
    public InputStream getArtifact(String path) {
        var artifactFile = new File(repositoryFile, path);
        if (artifactFile.exists()) {
            try {
                return new FileInputStream(artifactFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public void storeArtifact(InputStream artifactInputStream, String artifactName, String version, String type) throws IOException {
        var artifactFile = new File(repositoryFile, getArtifactPath(artifactName, version, type));
        var artifactDir = artifactFile.getParentFile();
        if (! artifactDir.exists())
            if (!artifactDir.mkdirs())
                throw new RuntimeException(String.format("Unable to create directory %s", artifactDir.getAbsolutePath()));
        if (artifactFile.exists())
            if (!artifactFile.delete())
                throw new RuntimeException(String.format("Unable to delete file %s", artifactFile.getAbsolutePath()));

        try (var artifactOutputStream = new FileOutputStream(artifactFile)) {
            var buf = new byte[1024];
            int len;
            while ((len = artifactInputStream.read(buf)) >= 0) {
                artifactOutputStream.write(buf, 0, len);
            }
        }
    }


    public static class Builder {

        private String repositoryPath = null;

        public Builder repositoryPath(final String repositoryPath) {
            this.repositoryPath = repositoryPath;
            return this;
        }

        public LocalBinaryRepository build() {
            File repositoryFile = repositoryPath == null ?
                    new File(String.format("%s/.m2/repository", System.getProperty("user.home"))) :
                    new File(repositoryPath);
            if (! repositoryFile.exists())
                if (!repositoryFile.mkdirs())
                    throw new RuntimeException(String.format("Unable to create directory %s", repositoryFile.getAbsolutePath()));
            return new LocalBinaryRepository(repositoryFile);
        }

    }
}
