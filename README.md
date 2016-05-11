# AjaxScaffold
Grails Plugin adding Ajax scaffold using Bootstrap3 (forked from kickstartWithBootstrap)

Features:
- Full compatible grails 2.4.*
- Replace old ressource plugin by asset-pipeline
- Add bootstrap-select (by Casey Holzer) for select widget (many-to-one, inList...)
- Use jQuery-Bootgrid (by Rafael Staib) with full ajax support in the default index scaffolding

# How to use:
- add the plugin (in case you use maven-install):
<pre>
	compile ":kickstart-with-bootstrap:1.3.0"
	// or
	grails.plugin.location.'kickstart-with-bootstrap' = "../KickstartWithBootstrap3"
</pre>
- add to Config.groovy desired date format for binding:
<pre>
	grails.databinding.dateFormats = [
		'dd/MM/yyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss'Z'"
	]
</pre>
- set default date format in your /i18n/messages.properties :
<pre>
default.date.format= dd/MM/yyyy 
</pre>
-then kickstart with bootstrap (overwrite if you should):
<pre>
	grails kickstart
</pre>

# Quick example:
- create a domain 'grails create-domain-class Person' and some attributes:
<pre>
	class Person(){
		Long id
		String firstName
		String lastName
		boolean isMarried
		Date birthDate
	  static constraints = { }	
	}
</pre>
- create a controller 'grails create-controller Person' and enable scaffold:
<pre>
	class PersonController {
		static scaffold = Person
		//def index() { }
		//if case you want to change search field
		def json(){
			setSearchField('firstName')
		} 
	}
</pre>
# enjoy!

