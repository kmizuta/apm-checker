package techarch.repository;

import lombok.EqualsAndHashCode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@EqualsAndHashCode
public class GitRepository implements AutoCloseable {

    final File localRepoDir;

    private GitRepository(final String remoteRepoUrl, final String username, final String password) {
        try {
            localRepoDir = Files.createTempDirectory("git").toFile();

            var cloneCommand = Git.cloneRepository()
                    .setURI(remoteRepoUrl)
                    .setDirectory(localRepoDir);
            if (username != null && password != null)
                cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            cloneCommand.call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public File getFile(final String path) {
        var file = new File(localRepoDir, path);
        return file.exists() ? file : null;
    }

    @Override
    public void close() {
        deleteDirectory(localRepoDir);
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static class Builder {

        private String remoteRepoUrl;
        private String username, password;

        public Builder remoteRepoUrl(final String remoteRepoUrl) {
            this.remoteRepoUrl = remoteRepoUrl;
            return this;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public GitRepository build() {
            return new GitRepository(remoteRepoUrl, username, password);
        }

    }


    public static void main(String[] args) throws IOException {
        File f = Files.createTempDirectory("foo").toFile();
System.out.println(f.getAbsolutePath());
System.out.println(System.getProperties().get("java.io.tmpdir"));
    }
}
