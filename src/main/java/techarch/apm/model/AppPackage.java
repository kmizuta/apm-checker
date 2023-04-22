package techarch.apm.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public class AppPackage {

    private String name;
    private String description;
    private String version;
    private Map<String, String> dependencies;

}
