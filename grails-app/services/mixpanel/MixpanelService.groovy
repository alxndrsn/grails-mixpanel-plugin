package mixpanel

import org.grails.plugin.platform.events.EventMessage
import org.grails.plugin.platform.events.EventReply
import grails.events.Listener

import org.json.JSONObject

import com.mixpanel.mixpanelapi.*

class MixpanelService {
	def grailsApplication
	def grailsEvents

	@Lazy private messageBuilder = new MessageBuilder(grailsApplication.config.grails.plugin.mixpanel.key)
	@Lazy private mixpanelApi = new MixpanelAPI()
	@Lazy private mixpanelHelperService = getMixpanelHelperService()
	@Lazy private distinctIdGetterName = getDistinctIdGetterName()
	@Lazy private deliverEventsToMixpanel = !(grailsApplication.config.grails.plugin.mixpanel.simulate instanceof Boolean?
			grailsApplication.config.grails.plugin.mixpanel.simulate: true)

	@Listener(namespace='mixpanel', topic='async')
	def asyncListener(MixpanelAsyncEvent event) {
		logEvent(event.namespace, event.eventName, event.eventObject)
	}

	def logEvent(namespace, eventName, eventObject) {
		if(deliverEventsToMixpanel) {
			try {
				def mixpanelMessage = messageBuilder.event(getDistinctId(), "$namespace::$eventName", createJson(eventObject))
				deliver(mixpanelMessage)
			} catch(Exception ex) {
				log.warn("Exception thrown while processing Mixpanel event.", ex)
			}
		} else {
			log.warn "Not sending mixpanel event due to config option grails.plugin.mixpanel.simulate set or defaulting to true: {namespace:$namespace, eventName:$eventName, eventObject:$eventObject}"
		}
	}

	def listenFor(namespace=null, eventName) {
		on(namespace, eventName) { eventObject ->
			logEvent(namespace, eventName, eventObject)
		}
	}

	def listenForAsync(namespace=null, eventName) {
		on(namespace, eventName) { eventObject ->
			// Wrap the event, and fire a new event to be processed on a separate thread
			grailsEvents.event('mixpanel', 'async', new MixpanelAsyncEvent(namespace, eventName, eventObject))
		}
	}

	def registerMixpanelListeners(conf) {
		if(!conf.standard && !conf.forceasync) {
			log.warn("NO MIXPANEL LISTENERS DEFINED. Please set grails.plugin.mixpanel.events.standards or grails.plugin.mixpanel.events.forceasync in Config.groovy if you want server side events to be logged")
		}
		if(conf.standard) {
			if(conf.standard instanceof Map) {
				conf.standard.each { namespace, events ->
					events.each { event -> listenFor(namespace, event) }
				}
			} else {
				conf.standard.each { event -> listenFor(event) }
			}
		}

		if(conf.forceasync) {
			if(conf.forceasync instanceof Map) {
				conf.forceasync.each { namespace, events ->
					events.each { event -> listenForAsync(namespace, event) }
				}
			} else {
				conf.forceasync.each { event -> listenForAsync(event) }
			}
		}
	}

	private getMixpanelHelperService() {
		def serviceName = grailsApplication.config.grails.plugin.mixpanel.helperService?: 'mixpanelHelperService'
		def service = grailsApplication.mainContext.getBean(serviceName)
		return service
	}

	private getDistinctIdGetterName() {
		def getterName = grailsApplication.config.grails.plugin.mixpanel.distinctIdGetterName?: 'getDistinctId'
	}

	private getDistinctId() {
		return mixpanelHelperService[distinctIdGetterName]()
	}

	private createJson(object) {
		object? new JSONObject('{'+object+'}'): null
	}

	private deliver(mixpanelMessage) {
		ClientDelivery delivery = new ClientDelivery()
		delivery.addMessage(mixpanelMessage)
		mixpanelApi.deliver(delivery)
	}
}

class MixpanelAsyncEvent {
	def namespace
	def eventName
	def eventObject

	MixpanelAsyncEvent(namespace, eventName, eventObject) {
		this.namespace = namespace
		this.eventName = eventName
		this.eventObject = eventObject
	}
}
