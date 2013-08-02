# Grails Mixpanel Plugin

## Intro

[Grails][1] integration for [Mixpanel][2] analytics.

Depends on [Grails Platform Core][3] for events.

## Setup

### Server-side

#### Registering listeners

If you want to use mixpanel on the server-side, you can register events using:

	mixpanelService.listenFor(namespace, eventName)
	mixpanelService.listenForAsync(namespace, eventName)

Or at compile-time:

	<Config.groovy>
	grails.plugin.mixpanel.events.standard = ...
	grails.plugin.mixpanel.events.forceasync = ...

These can either be a list of event names (e.g. `['login', 'logout']`, or a map of namespace->events (e.g. `[gorm:['afterInsert', 'afterDelete'], ...]`)

#### Firing events

Then fire events with the [Platform Core Events API][5]:

	event for:'security', topic:'userLogged', data:session.user

### Client-side

#### Enable

Add the following inside your GSP head tag:

	<mixpanel:headScript/>

or

	<script type="text/javascript">
		...
		<mixpanel:head/>
		...
	<script>

#### Use

To register an event, use the [standard mixpanel javascript API][4], e.g.

	mixpanel.identify(distinctId);
	mixpanel.people.set({...});
	mixpanel.track("some-event");



## Config

### grails.plugin.mixpanel.simulate

Set this to false if you want your events to be delivered to Mixpanel.  Otherwise they will be passed to `log.warn()` instead.  This prevents Mixpanel trials from expiring due to uninteresting events generated in development or testing.

Defaults to `true`.

### grails.plugin.mixpanel.key

This is your mixpanel API key.

### grails.plugin.mixpanel.standard

See Setup > Server-side

### grails.plugin.mixpanel.serializers

Allows you to set up custom closures that will be used to serialize objects for each namespace. Each should be a closure that takes an Object and returns a map, which the plugin will convert to JSON before sending. For example, to only send classname and id for all events in the gorm namespace:

	grailsApplication.config.grails.plugin.mixpanel.serializers.gorm = { obj -> return [id: obj.id, class: obj.class] }


### grails.plugin.mixpanel.defaultSerializer

This allows a default serializer to be used if no custom one is defined for the current event's namespace (see 'grailsApplication.config.grails.plugin.mixpanel.serializers'). This should be defined as a closure that takes a single Object argument and returns a map, which will be converted to JSON by the plugin. In the absernce of a defaultSerializer, the behaviour of the plugin is to use the Grails JSON serializer on the Object.


### grails.plugin.mixpanel.forceasync

See Setup > Server-side

### grails.plugin.mixpanel.helperService

This is a grails service which provides Mixpanel helper methods.  You should implement this in your app.  Default: `mixpanelHelperService`.

### grails.plugin.mixpanel.distinctIdGetterName

Name of the closure for getting the distinct ID for an event from the `helperService`.


## Spring Security Integration

To tie your server-side events to the current spring security user, try defining the following:

	class MixpanelHelperService {
		def springSecurityService

		def getDistinctId = {
			def userId = springSecurityService.currentUser?.id
			def distinctId = userId? "user-$userId": 'not-logged-in'
			return distinctId
		}
	}

[1]: http://grails.org
[2]: http://www.mixpanel.com
[3]: http://grails.org/plugin/platform-core
[4]: https://mixpanel.com/docs/integration-libraries/javascript-full-api
[5]: http://grailsrocks.github.com/grails-platform-core/guide/events.html


