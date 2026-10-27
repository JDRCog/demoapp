# Approved dependencies

| Group | Artifact | Scope | Notes |
|---|---|---|---|
| (JDK 17) | `java.xml` (`javax.xml.stream`, `javax.xml.parsers`, `javax.xml.xpath`) | compile | Use for all ISO 20022 XML |
| org.junit.jupiter | junit-jupiter | test | 5.10.x |
| org.assertj | assertj-core | test | 3.25.x |
| org.jacoco | jacoco-maven-plugin | build | 0.8.11 |
| org.apache.maven.plugins | maven-compiler-plugin | build | 3.13.x |
| org.apache.maven.plugins | maven-surefire-plugin | build | 3.2.x |

Anything not listed (e.g. Prowide, JAXB runtimes, Jackson XML) requires ARB approval.
