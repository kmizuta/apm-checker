package techarch.slaps;

import com.github.mustachejava.DefaultMustacheFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import techarch.repository.Artifactory;
import techarch.repository.GitRepository;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RicChecker {

    public static void main(String[] args) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException, XmlPullParserException {
        var checker = new RicChecker();
        checker.run2();
    }

    public void run2() throws IOException, XmlPullParserException {
        var git = GitRepository.builder().remoteRepoUrl("ssh://kenichi.mizuta%40oracle.com@alm.oraclecorp.com/fusionapps_helidon-3rdparty_28494/helidon-3rdparty.git").build();
        var helidonUsagesPom = git.getFile("pom.xml");
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader(helidonUsagesPom));

        final var dependencies = new Dependencies(new ArrayList<>());
        model.getDependencies().forEach( d -> {
            dependencies.getDependencies().add(new Dependency(d.getGroupId(), d.getArtifactId()));
            System.out.println(d.getArtifactId());
        });


        System.out.println(model.getId());
        System.out.println(model.getGroupId());
        System.out.println(model.getArtifactId());
        System.out.println(model.getVersion());

        var mustacheFactory = new DefaultMustacheFactory();
        var mustache = mustacheFactory.compile("template." +
                "pom.xml");
        mustache.execute(new PrintWriter(System.out), dependencies).flush();
    }
    public void run() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException { // https://repo1.maven.org/maven2/io/helidon/helidon-bom/3.2.0/helidon-bom-3.2.0.pom
        var docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var xpathFactory = XPathFactory.newInstance();
        var xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if ("maven".equals(prefix))
                    return "http://maven.apache.org/POM/4.0.0";
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<String> getPrefixes(String namespaceURI) {
                return null;
            }
        });
        var dependencyExpression = xpath.compile("/project/dependencies/dependency");
        var dependencyManagementExpression = xpath.compile("/project/dependencyManagement/dependencies/dependency");
        var groupExpression = xpath.compile("groupId");
        var artifactExpression = xpath.compile("artifactId");


        var git = GitRepository.builder().remoteRepoUrl("ssh://kenichi.mizuta%40oracle.com@alm.oraclecorp.com/fusionapps_helidon-3rdparty_28494/helidon-3rdparty.git").build();
        var helidonUsagesPom = git.getFile("pom.xml");
        var doc = docBuilder.parse(helidonUsagesPom);
        NodeList list = (NodeList) dependencyExpression.evaluate(doc, XPathConstants.NODESET);
        int len = list.getLength();
        for (int i=0; i<len; i++) {
            var dependency = list.item(i);
            var node = (Element)groupExpression.evaluate(dependency, XPathConstants.NODE);
            System.out.println(node.getTextContent());
            node = (Element)artifactExpression.evaluate(dependency, XPathConstants.NODE);
            System.out.println("\t"+node.getTextContent());
        }

//https://artifactory.oci.oraclecorp.com/fa-spectra-lib-snapshot-maven-local/oracle/spectra/
        // https://artifactory.oci.oraclecorp.com/fa-spectra-lib-snapshot-maven-local/oracle/spectra/infra-bom/2304.0.6/infra-bom-2304.0.6.pom


        var artifactory = Artifactory.builder()
                .repositoryUrl("https://artifactory.oci.oraclecorp.com/fa-spectra-lib-snapshot-maven-local")
                .build();
        var bomPom = artifactory.getArtifact("oracle.spectra", "infra-bom", "2304.0.6", "pom");
        doc = docBuilder.parse(bomPom);
        list = (NodeList) dependencyManagementExpression.evaluate(doc, XPathConstants.NODESET);
        len = list.getLength();
        for (int i=0; i<len; i++) {
            var dependency = list.item(i);
            var node = (Element)groupExpression.evaluate(dependency, XPathConstants.NODE);
            System.out.println(node.getTextContent());
            node = (Element)artifactExpression.evaluate(dependency, XPathConstants.NODE);
            System.out.println("\t"+node.getTextContent());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Dependencies {
        private List<Dependency> dependencies;
    }

    @Getter
    @AllArgsConstructor
    public static class Dependency {
        private String groupId, artifactId;
    }
}
