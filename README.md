# SQL generator

# Description

...

# Configuration

## sqlgen::generate

    <plugin>
        <groupId>com.backbase.oss</groupId>
        <artifactId>blimp-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <executions>
            <execution>
                <goals>
                    <goal>generate</goal>
                </goals>
                <configuration>
                    <databases>
                        <database>mariadb</database>
                        <database>mssql</database>
                        <database>mysql</database> <!-- default -->
                        <database>oracle</database>
                    </databases>
                    <changeLogFile>db.changelog-persistence.xml</changeLogFile>
                    <inputDirectory>${project.basedir}/src/main/resources</inputDirectory>
                    <outputDirectory>${project.build.directory}/generated-resources/liquibase</outputDirectory>
                    <serviceName>${project.artifactId}</serviceName>
                    <addResource>false</addResource>
                    <format>zip</format> <!-- it doesn't generate any assembly if not specified -->
                    <classifier>sql</classifier> <!-- if format is specified -->
                </configuration>
            </execution>
        </executions>
    </plugin>
