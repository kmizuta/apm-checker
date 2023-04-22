package techarch.apm;

public class VersionUtil {

    public static String toExactVersion(final String version) {
        return (version != null && version.startsWith(">=")) ? version.substring(2) : version;
    }

}
