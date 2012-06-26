/**
 * Project build configuration
 * @Author  Jeroen Wesbeek
 * @Since   20091027
 * @Description
 *
 * Specific build configuration for the GSCF application
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

//grails.plugin.repos.discovery.intient = "http://intient.com/downloads/grails/"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
		mavenCentral()
		mavenRepo "http://nexus.nmcdsp.org/content/repositories/releases"
//		mavenRepo "http://snapshots.repository.codehaus.org"
		mavenRepo "http://repository.codehaus.org"
		mavenRepo "http://download.java.net/maven/2/"
		mavenRepo "http://repository.jboss.org"
		mavenRepo "http://maven.nuxeo.org/nexus/content/repositories/public"

        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
//		build 'org.codehaus.gpars:gpars:0.11'
		runtime 'postgresql:postgresql:9.1-901.jdbc3'
		compile 'org.apache.poi:poi:3.7'
		compile 'org.apache.poi:poi-ooxml:3.7'
		compile 'org.apache.poi:poi-ooxml-schemas:3.7'
//		compile 'xmlbeans:xbean:2.2.0'

//		compile 'org.apache.poi:poi:3.7'
//		compile 'org.apache.poi:poi-ooxml:3.7'
//		compile 'org.apache.poi:poi-ooxml-schemas:3.7'
//		http://repo1.maven.org/maven2/xmlbeans/xbean/2.2.0/xbean-2.2.0.jar
//		http://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/3.7/poi-ooxml-3.7.jar
//		https://maven.nuxeo.org/nexus/content/repositories/public/org/jrobin/jrobin/1.5.9/jrobin-1.5.9.jar
    }
	plugins {
		compile(":hibernate:$grailsVersion")
		compile ':tomcat:1.3.7.2'
		compile ':jquery:latest.integration'
		compile ':grom:latest.integration'
		compile ':ajaxflow:latest.integration'
		compile ':crypto:2.0'
		compile ':spring-security-core:1.1.2'
		compile(':gdt:0.2.2') {
			// disable plugin dependency transition because it's horribly broken
			// note: this assumes that ajaxflow, jquery and cryto stay included
			transitive = false
		}

        compile(':gdtimporter:0.4.6.7') {
            // see comment above on gdt, gdtimporter also requires
            // spring security core (and shouldn't really be a plugin)
            transitive = false
        }
		compile ':famfamfam:1.0.1'
		compile ':jumpbar:0.1.5'
		compile ':mail:1.0'

		compile ':webflow:1.3.7'

//		compile ':trackr:0.6.4'
//		runtime ':grails-melody:1.11'
	}
}

//grails.plugin.location.'grom' = '../grom'
//grails.plugin.location.'grom' = '../../4np/grails-grom'
//grails.plugin.location.'ajaxflow' = '../ajaxflow'
//grails.plugin.location.'ajaxflow' = '../../4np/grails-ajaxflow'
//grails.plugin.location.'gdt' = '../gdt'
//grails.plugin.location.'jumpbar' = '../jumpbar'
//grails.plugin.location.'gdtimporter' = '../gdtimporter'
