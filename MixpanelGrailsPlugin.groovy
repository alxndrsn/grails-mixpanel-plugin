class MixpanelGrailsPlugin {
	// the plugin version
	def version = "0.1-SNAPSHOT"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.0 > *"
	// the other plugins this plugin depends on
	def dependsOn = [:]
	// resources that are excluded from plugin packaging
	def pluginExcludes = [
		"grails-app/views/error.gsp"
	]

	def title = "Mixpanel Plugin" // Headline display name of the plugin
	def author = "Alex Anderson"
	def authorEmail = ""
	def description = 'Integration with www.mixpanel.com analytics'
	def documentation = "http://github.com/alxndrsn/grails-mixpanel-plugin"
	def license = "APACHE"

	def doWithWebDescriptor = { xml ->
	}

	def doWithSpring = {
	}

	def doWithDynamicMethods = { ctx ->
	}

	def doWithApplicationContext = { applicationContext ->
		def mixpanelService = applicationContext.mixpanelService
		if(!mixpanelService.grailsApplication) mixpanelService.grailsApplication = application
		// TODO this needs to be done in the plugin, and in a SERVICE so that `on` method is available
		// May either be a list ['x', 'y', 'z'], or a Map [namespace1:['events']]...
		def conf = application.config.grails.plugin.mixpanel.events
		mixpanelService.registerMixpanelListeners(conf)
	}

	def onChange = { event ->
	}

	def onConfigChange = { event ->
	}

	def onShutdown = { event ->
	}
}

