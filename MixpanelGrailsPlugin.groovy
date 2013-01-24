class MixpanelGrailsPlugin {
	def version = "0.1-SNAPSHOT"
	def grailsVersion = "2.0 > *"
	def title = "Mixpanel Plugin"
	def author = "Alex Anderson"
	def authorEmail = ""
	def description = 'Integration with www.mixpanel.com analytics'
	def documentation = "http://github.com/alxndrsn/grails-mixpanel-plugin"
	def license = "APACHE"
	def issueManagement = [system: "Github", url: "https://github.com/alxndrsn/grails-mixpanel-plugin/issues"]
	def scm = [url: "https://github.com/alxndrsn/grails-mixpanel-plugin"]

	def doWithApplicationContext = { applicationContext ->
		def mixpanelService = applicationContext.mixpanelService
		if(!mixpanelService.grailsApplication) mixpanelService.grailsApplication = application
		// TODO this needs to be done in the plugin, and in a SERVICE so that `on` method is available
		// May either be a list ['x', 'y', 'z'], or a Map [namespace1:['events']]...
		def conf = application.config.grails.plugin.mixpanel.events
		mixpanelService.registerMixpanelListeners(conf)
	}
}
