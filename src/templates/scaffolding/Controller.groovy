<%=packageName ? "package ${packageName}\n\n" : ''%>

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.*

/**
 * ${className}Controller
 * A controller class handles incoming web requests and performs actions such as redirects, rendering views and so on.
 */
@Transactional(readOnly = false)
class ${className}Controller {

	def excludedProps = ['version','dateCreated','lastUpdated', 'springSecurityService']
	
	String NO_DATA = '''{
		  "current": 0,
		  "rowCount": 0,
		  "rows": [],
		  "total": 0
		}'''

	def rows
	
	def index(){
		respond new ${className}(params)
	}
	
	def registerObjectMarshallerCharacter(){
		JSON.registerObjectMarshaller(Character) {
			return it?.toString()
		}
	}
	
	def registerObjectMarshallerDate(dateFormat){
		JSON.registerObjectMarshaller(Date) {
			return it?.format(dateFormat)
		}
	}
	
	def registerObjectMarshallerList(${className} ${propertyName}){
		Map result = [:]
		def domain = new DefaultGrailsDomainClass(${className})
		domain.properties.each {GrailsDomainClassProperty property ->
			if (!(property.name in excludedProps))
				result[property.name] = property.isAssociation()? ${propertyName}[property.name].toString():${propertyName}[property.name]
		}
		return result
	}
	def registerObjectMarshallerEdit(${className} ${propertyName}){
		Map result = [:]
		def domain = new DefaultGrailsDomainClass(${className})
		domain.properties.each {GrailsDomainClassProperty property ->
			if (!(property.name in excludedProps))
				result[property.name] = ${propertyName}[property.name]
		}
		return result
	}
	
	def registerObjectMarshallerEnum(){
		JSON.registerObjectMarshaller(Enum) { Enum someEnum ->
			someEnum?.name()
		}
	}
	
	def setSearchField(def searchField){
		
		registerObjectMarshallerCharacter()
		
		registerObjectMarshallerDate( getDefaultDateFormat() )
		
		registerObjectMarshallerEnum()
		
		JSON.registerObjectMarshaller(${className}) { ${className} ${propertyName} ->
			return registerObjectMarshallerList(${propertyName})
		}		
	
		def search = params['searchPhrase']
		def rowCount = params['rowCount'] == null?0:params['rowCount'] as int
		def current = params['current'] == null?0:(params['current'] as int) - 1
		def total = ${className}.count()

		if (total == 0){
			render NO_DATA
		} else {
			def sorts = params.collectEntries {
				if (it.toString().startsWith("sort")){
					def s = it.toString()
					[(s.substring(s.indexOf('[')+1,s.indexOf(']'))):s.substring(s.indexOf('=')+1)]
				} else [:]
			}.grep()
			
			rows = ${className}.createCriteria().list{
				sorts.each {
					order(it.key, it.value)
				}
				firstResult(current*rowCount)
				if (rowCount > 0)
					maxResults(rowCount)
				or {
					if (search){
						if (searchField == 'id' || search.isNumber()){
							eq(searchField, search.toString().toLong())
						} else
							ilike(searchField, '%' +search.toString()+ '%')
					}
				}
			}
			def jsonRows = rows as JSON
			render '{"current":' +(current+1)+ ',"rowCount":' +rowCount+ ',"rows":' +jsonRows.toString()+ ',"total": ' +total+ '}'
		} //if		
		
	}
	
	def json(){
	
		setSearchField('id')		
		
	} //json
	
	String getDefaultDateFormat(){
		message(code:"default.date.format", default: java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).pattern.replace('yy', 'yyyy'), locale:request.locale )
	}
	
	def delete(){
		def ${propertyName}Instance = ${className}.get(params['id'])
		try {
			${propertyName}Instance.delete(flush: true)
		} catch (Exception e) {
			render e.message
			return
		}
		if (params['action'] == 'delete'){
			render params['id'] // + " deleted."
		} else {
			request.withFormat {
				form {
					flash.message = message(code: 'default.deleted.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
					redirect action:"index", method:"GET"
				}
				'*'{ render status: NO_CONTENT }
			}
		}			
	} // delete()

	def editJson(){
		JSON.registerObjectMarshaller(${className}) { ${className} ${propertyName} ->
			return registerObjectMarshallerEdit(${propertyName})
		}		
		render (contentType: "text/json") {
			${className}.get(params['edit'])
		}
	} // editJson()
	
	def updateJson(${className} ${propertyName}){
		// def ${propertyName}Instance
		/* if (params['mode'] =='update'){
			${propertyName}Instance = ${className}.get(params['id'])

		} else  if (params['mode'] =='create') {
			${propertyName} Instance = new ${className}()
		}*/
		// ${propertyName}Instance.properties = params
		// bindData(${propertyName}Instance, params, [exclude: ['id', 'mode', '_']])
		
		${propertyName}.save(flush: true)
		if (${propertyName}.hasErrors()){
			render ${propertyName}.errors.allErrors.collect {
				message(error:it, encodeAs:'HTML')
			} as JSON
		} else {
			render ''// 'Object '+params['mode']+'d successfully.'
		}
	} // update()
	

	def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond ${className}.list(params), model:[${propertyName}Count: ${className}.count()]
    }

    def show(${className} ${propertyName}) {
        respond ${propertyName}
    }

    def create() {
        respond new ${className}(params)
    }

    @Transactional
    def save(${className} ${propertyName}) {
        if (${propertyName} == null) {
            notFound()
            return
        }

        if (${propertyName}.hasErrors()) {
            respond ${propertyName}.errors, view:'create'
            return
        }

        ${propertyName}.save flush:true

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: '${propertyName}.label', default: '${className}'), ${propertyName}.id])
                redirect ${propertyName}
            }
            '*' { respond ${propertyName}, [status: CREATED] }
        }
    }

    def edit(${className} ${propertyName}) {
        respond ${propertyName}
    }

    @Transactional
    def update(${className} ${propertyName}) {
        if (${propertyName} == null) {
            notFound()
            return
        }

        if (${propertyName}.hasErrors()) {
            respond ${propertyName}.errors, view:'edit'
            return
        }

        ${propertyName}.save flush:true

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                redirect ${propertyName}
            }
            '*'{ respond ${propertyName}, [status: OK] }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: '${propertyName}.label', default: '${className}'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }	

/*
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

	def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond ${className}.list(params), model:[${propertyName}Count: ${className}.count()]
    }


    @Transactional
    def delete(${className} ${propertyName}) {

        if (${propertyName} == null) {
            notFound()
            return
        }

        ${propertyName}.delete flush:true

        request.withFormat {
            form {
                flash.message = message(code: 'default.deleted.message', args: [message(code: '${className}.label', default: '${className}'), ${propertyName}.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }


	
	*/
}
