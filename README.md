# AjaxScaffold
Grails Plugin adding Ajax scaffold using Bootstrap3 (forked from kickstartWithBootstrap)

Features:
- Full compatible grails 2.4.*
- Replace old ressource plugin by asset-pipeline
- Add bootstrap-select (by Casey Holzer) for select widget (many-to-one, inList...)
- Use jQuery-Bootgrid (by Rafael Staib) with full ajax support in the default index scaffolding

How to use:
# add the plugin (in case you use maven-install):
compile ":kickstart-with-bootstrap:1.3.0"
or
grails.plugin.location.'kickstart-with-bootstrap' = "../KickstartWithBootstrap3"

# add to Config.groovy desired date format for binding:
grails.databinding.dateFormats = [
	'dd/MM/yyyy', 'yyyy-MM-dd HH:mm:ss.S', "yyyy-MM-dd'T'hh:mm:ss'Z'"
]

# set default date format in your /i18n/messages.properties :
default.date.format= dd/MM/yyyy 

# Then kickstart with bootstrap (overwrite if you should):
grails kickstart

# Quick example:
# create a domain 'grails create-domain-class Person' and some attributes:
class Person(){
	Long id
	String firstName
	String lastName
	boolean isMarried
	Date birthDate
  static constraints = {
  }	
}
# create a controller 'grails create-controller Person' and enable scaffold:
class PersonController {

	static scaffold = Person
//	def index() { }
  
  //if case you want to change search field
  def json(){
    setSearchField('firstName')
  }  
}

#enjoy!
