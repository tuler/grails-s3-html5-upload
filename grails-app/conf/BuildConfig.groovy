grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
grails.project.repos.vidergize.url = "http://nexus.vidergize.com/content/repositories/releases"
grails.project.repos.vidergize.username = "deployment"
grails.project.repos.vidergize.password = "v1derg1ze"
grails.project.repos.default = "vidergize"
grails.release.scm.enabled = false

grails.project.dependency.resolution = {
	// inherit Grails' default dependencies
	inherits("global") {
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility
	repositories {
		grailsCentral()
	}
	dependencies {
	}
	
	plugins {
		build(":tomcat:$grailsVersion",
			":release:2.2.1",
			":rest-client-builder:1.0.3") {
			export = false
		}
	}
}
