package kickstart

import java.text.DateFormat
import java.text.DateFormatSymbols

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.servlet.support.RequestContextUtils as RCU

class BootstrapTagLib {
	static namespace = "bs"
	static defaultEncodeAs = [taglib:'text']

	def requestDataValueProcessor
	def messageSource

	def paginate = { attrs ->
		def writer = out
		if (attrs.total == null) {
			throwTagError("Tag [paginate] is missing required attribute [total]")
		}

		def locale			= RCU.getLocale(request)

		def total			= attrs.int('total')		?: 0
		def action			= (attrs.action ? attrs.action : (params.action ? params.action : "list"))
		def offset			= params.int('offset')		?: 0
		def max				= params.int('max')
		def maxsteps		= (attrs.int('maxsteps')	?: 10)

		if (!offset)offset	= (attrs.int('offset')		?: 0)
		if (!max)	max		= (attrs.int('max')			?: 10)

		def linkParams = [:]
		if (attrs.params)	linkParams.putAll(attrs.params)
		linkParams.offset = offset - max
		linkParams.max = max
		if (params.sort)	linkParams.sort		= params.sort
		if (params.order)	linkParams.order	= params.order

		def linkTagAttrs = [action:action]
		if (attrs.controller)		linkTagAttrs.controller = attrs.controller
		if (attrs.id != null)		linkTagAttrs.id = attrs.id
		if (attrs.fragment != null)	linkTagAttrs.fragment = attrs.fragment
		linkTagAttrs.params = linkParams

		// determine paging variables
		def steps 		= maxsteps > 0
		int currentstep	= (offset / max) + 1
		int firststep	= 1
		int laststep	= Math.round(Math.ceil(total / max))

		// display previous link when not on firststep
		def disabledPrev = (currentstep > firststep) ? "" : "disabled"
//		linkTagAttrs.class = 'prevLink'
//		linkParams.offset = offset - max
		writer << "<ul class='pagination'>"
		writer << "<li class='prev ${disabledPrev}'>"
		writer << link(linkTagAttrs.clone()) {
			(attrs.prev ?: messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
		}
		writer << "</li>"

		// display steps when steps are enabled and laststep is not firststep
		if (steps && laststep > firststep) {
			linkTagAttrs.class = 'step'

			// determine begin and endstep paging variables
			int beginstep	= currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
			int endstep		= currentstep + Math.round(maxsteps / 2) - 1
			if (beginstep < firststep) {
				beginstep = firststep
				endstep = maxsteps
			}
			if (endstep > laststep) {
				beginstep = laststep - maxsteps + 1
				if (beginstep < firststep) {
					beginstep = firststep
				}
				endstep = laststep
			}

			// display firststep link when beginstep is not firststep
			if (beginstep > firststep) {
				linkParams.offset = 0
				writer << "<li>"
				writer << link(linkTagAttrs.clone()) {firststep.toString()}
				writer << "</li>"
				writer << '<li class="disabled"><a href="#">…</a></li>'
			}

			// display paginate steps
			(beginstep..endstep).each { i ->
				if (currentstep == i) {
					writer << "<li class='active'><a href='#'>"+i.toString()+"</a></li>"
				}
				else {
					linkParams.offset = (i - 1) * max
					writer << "<li>"
					writer << link(linkTagAttrs.clone()) {i.toString()}
					writer << "</li>"
				}
			}

			// display laststep link when endstep is not laststep
			if (endstep < laststep) {
				linkParams.offset = (laststep -1) * max
				writer << '<li class="disabled"><a href="#">…</a></li>'
				writer << "<li>"
				writer << link(linkTagAttrs.clone()) { laststep.toString() }
				writer << "</li>"
			}
		}

		// display next link when not on laststep
		def disabledNext = (currentstep < laststep) ? "" : "disabled"
		linkParams.offset = (currentstep)*max
		writer << "<li class='next ${disabledNext}'>"
		writer << link(linkTagAttrs.clone()) {
			(attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
		}
		writer << "</li>"
		writer << "</ul>"
	}


	/**
	 * A simple date picker that renders a date as selects.<br/>
	 * This is just an initial hack - can be widely improved!
	 * e.g. &lt;bs:datePicker name="myDate" value="${new Date()}" /&gt;
	 *
	 * @emptyTag
	 *
	 * @attr name REQUIRED The name of the date picker field set
	 * @attr value The current value of the date picker; defaults to now if not specified
	 * @attr precision The desired granularity of the date to be rendered
	 * @attr noSelection A single-entry map detailing the key and value to use for the "no selection made" choice in the select box. If there is no current selection this will be shown as it is first in the list, and if submitted with this selected, the key that you provide will be submitted. Typically this will be blank.
	 * @attr years A list or range of years to display, in the order specified. i.e. specify 2007..1900 for a reverse order list going back to 1900. If this attribute is not specified, a range of years from the current year - 100 to current year + 100 will be shown.
	 * @attr relativeYears A range of int representing values relative to value. For example, a relativeYears of -2..7 and a value of today will render a list of 10 years starting with 2 years ago through 7 years in the future. This can be useful for things like credit card expiration dates or birthdates which should be bound relative to today.
	 * @attr id the DOM element id
	 * @attr disabled Makes the resulting inputs and selects to be disabled. Is treated as a Groovy Truth.
	 * @attr readonly Makes the resulting inputs and selects to be made read only. Is treated as a Groovy Truth.
	 */
	def datePicker = { attrs ->
		def out = out // let x = x ?
		def inputClasses = attrs['class']
		def xdefault = attrs['default']
		def required = attrs['required']?attrs.required:'false'
		if (xdefault == null) {
			xdefault =  new Date()
		} else if (xdefault.toString() != 'none') {
			if (xdefault instanceof String) {
				xdefault = DateFormat.getInstance().parse(xdefault)
			} else if (!(xdefault instanceof Date)) {
				throwTagError("Tag [datePicker] requires the default date to be a parseable String or a Date")
			}
		} else {
			xdefault = null
		}
		def years = attrs.years
		def relativeYears = attrs.relativeYears
		if (years != null && relativeYears != null) {
			throwTagError 'Tag [datePicker] does not allow both the years and relativeYears attributes to be used together.'
		}

		if (relativeYears != null) {
			if (!(relativeYears instanceof IntRange)) {
				// allow for a syntax like relativeYears="[-2..5]". The value there is a List containing an IntRage.
				if ((!(relativeYears instanceof List)) || (relativeYears.size() != 1) || (!(relativeYears[0] instanceof IntRange))){
					throwTagError 'The [datePicker] relativeYears attribute must be a range of int.'
				}
				relativeYears = relativeYears[0]
			}
		}
		def value = attrs.value
		if (value.toString() == 'none') {
			value = null
		} else if (!value) {
			value = xdefault
		}
		def name = attrs.name
		def id = attrs.id ?: name

		def noSelection = attrs.noSelection
		if (noSelection != null) {
			noSelection = noSelection.entrySet().iterator().next()
		}

		final PRECISION_RANKINGS = ["year": 0, "month": 10, "day": 20, "hour": 30, "minute": 40]
		def precision = (attrs.precision ? PRECISION_RANKINGS[attrs.precision] :
			(grailsApplication.config.grails.tags.datePicker.default.precision ?
				PRECISION_RANKINGS["${grailsApplication.config.grails.tags.datePicker.default.precision}"] :
				PRECISION_RANKINGS["minute"]))

		def day
		def month
		def year
		def hour
		def minute
		def dfs = new DateFormatSymbols(RCU.getLocale(request))

		def c = null
		if (value instanceof Calendar) {
			c = value
		} else if (value != null) {
			c = new GregorianCalendar()
			c.setTime(value)
		}

		if (c != null) {
			day = c.get(GregorianCalendar.DAY_OF_MONTH)
			month = c.get(GregorianCalendar.MONTH) + 1		// add one, as Java stores month from 0..11
			year = c.get(GregorianCalendar.YEAR)
			hour = c.get(GregorianCalendar.HOUR_OF_DAY)
			minute = c.get(GregorianCalendar.MINUTE)
		}

		if (years == null) {
			def tempyear

			if (year == null) {
				// If no year, we need to get current year to setup a default range... ugly
				def tempc = new GregorianCalendar()
				tempc.setTime(new Date())
				tempyear = tempc.get(GregorianCalendar.YEAR)
			} else {
				tempyear = year
			}

			if (relativeYears) {
				if (relativeYears.reverse) {
					years = (tempyear + relativeYears.toInt)..(tempyear + relativeYears.fromInt)
				} else {
					years = (tempyear + relativeYears.fromInt)..(tempyear + relativeYears.toInt)
				}
			} else {
				years = (tempyear - 100)..(tempyear + 100)
			}
		}

		booleanToAttribute(attrs, 'disabled')
		booleanToAttribute(attrs, 'readonly')

		// get the localized format for dates. NOTE: datepicker only uses Lowercase syntax and does not understand hours, seconds, etc. (it uses: dd, d, mm, m, yyyy, yy)
 		String dateFormat = messageSource.getMessage("default.date.datepicker.format",null,null,LocaleContextHolder.locale )
		if (!dateFormat) { // if date.datepicker.format is not used use date.format but remove characters not used by datepicker
			dateFormat = messageSource.getMessage("default.date.format",null,'mm/dd/yyyy',LocaleContextHolder.locale )\
				.replace('z', '').replace('Z', '')\
				.replace('h', '').replace('H', '')\
				.replace('k', '').replace('K', '')\
				.replace('w', '').replace('W', '')\
				.replace('s', '').replace('S', '')\
				.replace('m', '').replace('a', '').replace('D', '').replace('E', '').replace('F', '').replace('G', '').replace(':', '')\
				.replace('MMM', 'MM').replace('ddd', 'dd')\
				.trim()\
				.toLowerCase()
		}
		String formattedDate = g.formatDate(format: dateFormat.replace('m', 'M'), date: c?.getTime())
		out.println "	<input id=\"${id}\" name=\"${name}\" class=\"datepicker ${inputClasses}\" size=\"16\" type=\"text\" value=\"${formattedDate}\" data-date-format=\"${dateFormat}\" "+(required=='true'?'required="required"':'')+"/>"
	}

	/**
	 * A fix for Grails's datePicker to use class styling
	 * based on http://grails.1312388.n4.nabble.com/How-to-set-css-classes-for-lt-g-datePicker-gt-td4242497.html
	 */
	def customDatePicker = {attrs, body ->
		def selectClass	= attrs['class']
		def unstyled	= g.datePicker(attrs, body)
		def styled		= unstyled.replaceAll('name="\\S+_(day|month|year|hour|minute)"') { match, index ->
			"${match} class=\"${selectClass}\""
		}
		out << styled
	}


	/**
	* A helper tag for creating checkboxes.
	 * example: 	<bs:checkBox name="sendEmail" value="${false}" onLabel="On" offLabel="Off"/>
	 * @emptyTag
	 *
	 * @attr name REQUIRED the name of the checkbox
	 * @attr value the value of the checkbox
	 * @attr checked if evaluates to true sets to checkbox to checked
	 * @attr onLabel the I18N code (or the text itself if not defined) to label the On/Yes/True button
	 * @attr offLabel the I18N code (or the text itself if not defined) to label the Off/No/False button
	 * @attr disabled if evaluates to true sets to checkbox to disabled
	 * @attr readonly if evaluates to true, sets to checkbox to read only
	 * @attr id DOM element id; defaults to name
	 */
	 def checkBox = { attrs ->
		def locale			= RCU.getLocale(request)

		def value		= attrs.remove('value')
		def label		= attrs['label']?attrs.label:''
		def name		= attrs.remove('name')
		def onLabel		= attrs.remove('onLabel')  ?: "checkbox.on.label"
		def offLabel	= attrs.remove('offLabel') ?: "checkbox.off.label"
		booleanToAttribute(attrs, 'disabled')
		booleanToAttribute(attrs, 'readonly')

		// Deal with the "checked" attribute. If it doesn't exist, we
		// default to a value of "true", otherwise we use Groovy Truth
		// to determine whether the HTML attribute should be displayed or not.
		def checked = true
		def checkedAttributeWasSpecified = false
		if (attrs.containsKey('checked')) {
			checkedAttributeWasSpecified = true
			checked = attrs.remove('checked')
		}

		if (checked instanceof String) checked = Boolean.valueOf(checked)

		if (value == null) value = false
		def hiddenValue = ""

		value = processFormFieldValueIfNecessary(name, value,"checkbox")
		hiddenValue = processFormFieldValueIfNecessary("_${name}", hiddenValue, "hidden")

//		out << """
//		<div>
//			<label for=\"_${name}\" class="control-label">
//				${messageSource.getMessage(name + '.label', null, '', locale)}
//			</label>
//
//			<div class="">
//"""

		out << "				<input type=\"hidden\" name=\"_${name}\""
		if(hiddenValue != "") {
			out << " value=\"${hiddenValue}\""
		}
		out << " />\n				<input class='hide pull-right' type=\"checkbox\" name=\"${name}\" "
		if (checkedAttributeWasSpecified) {
			if (checked) {
				out << 'checked="checked" '
			}
		}
		else if (value && value != "") {
			out << 'checked="checked" '
			checked = true
		}

		def outputValue = !(value instanceof Boolean || value?.class == boolean.class)
		if (outputValue) {
			out << "value=\"${value}\" "
		}
		// process remaining attributes
		outputAttributes(attrs, out)

		if (!attrs.containsKey('id')) {
			out << """id="${name}" """
		}

		// close the tag, with no body
		out << ' />'

		if (label)
			out << "<label for=\"${name}\" class=\"control-label\">${label}</label>"
			
		out << """
				<div id="btn-group-${name}" class="btn-group radiocheckbox" data-toggle="buttons-radio">
				"""
		out << """
					<button class="btn btn-sm on${value? ' active btn-primary' : ''}">${messageSource.getMessage(onLabel, null, onLabel, locale)}</button>
					<button class="btn btn-sm off${!value? ' active btn-primary' : ''}">${messageSource.getMessage(offLabel, null, offLabel, locale)}</button>
				</div>
		"""
	}

	 /**
	  * Dump out attributes in HTML compliant fashion.
	  */
	void outputAttributes(attrs, writer, boolean useNameAsIdIfIdDoesNotExist = false) {
		attrs.remove('tagName') // Just in case one is left
		attrs.each { k, v ->
			if(v != null) {
				writer << k
				writer << '="'
				writer << v.encodeAsHTML()
				writer << '" '
			}
		}
		if (useNameAsIdIfIdDoesNotExist) {
			outputNameAsIdIfIdDoesNotExist(attrs, writer)
		}
	}

	/**
	 * getter to obtain RequestDataValueProcessor from
	 */
    private getRequestDataValueProcessor() {
        if (requestDataValueProcessor == null && grailsAttributes.getApplicationContext().containsBean("requestDataValueProcessor")){
            requestDataValueProcessor = grailsAttributes.getApplicationContext().getBean("requestDataValueProcessor")
        }
        return requestDataValueProcessor
    }

	 private processFormFieldValueIfNecessary(name, value, type) {
		 def requestDataValueProcessor = getRequestDataValueProcessor()
		 def processedValue = value
		 if(requestDataValueProcessor != null) {
			 processedValue = requestDataValueProcessor.processFormFieldValue(request, name, "${value}", type)
		 }
		 return processedValue
	 }

	/**
	* Some attributes can be defined as Boolean values, but the html specification
	* mandates the attribute must have the same value as its name. For example,
	* disabled, readonly and checked.
	*/
	private void booleanToAttribute(attrs, String attrName) {
		def attrValue = attrs.remove(attrName)
		// If the value is the same as the name or if it is a boolean value,
		// reintroduce the attribute to the map according to the w3c rules, so it is output later
		if (Boolean.valueOf(attrValue) ||
		  (attrValue instanceof String && attrValue?.equalsIgnoreCase(attrName))) {
			attrs.put(attrName, attrName)
		} else if (attrValue instanceof String && !attrValue?.equalsIgnoreCase('false')) {
			// If the value is not the string 'false', then we should just pass it on to
			// keep compatibility with existing code
			attrs.put(attrName, attrValue)
		}
	}
	
	def jumbotron = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def title = attrs['title']?attrs.title:''
		out << "<div class='jumbotron"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		if (title) out << "<h1>${title}</h1>"
		out << body()
		out << '</div>'
	}
	
	def button = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def name = attrs['name']?attrs.name:''
		def onclick = attrs['onclick']?attrs.onclick:''
		def style = attrs['style']?attrs.style:'default'
		def type = attrs['type']?attrs.type:'button'
		def btSize = attrs['size']?attrs.size:''
		def target = attrs['target']?attrs.target:''
		def toggle = attrs['toggle']?attrs.toggle:''
		def dismiss = attrs['dismiss']?attrs.dismiss:''
		out << "<button type=\"${type}\" "+(dismiss?" data-dismiss=\"${dismiss}\"":'')+(toggle?" data-toggle=\"${toggle}\"":'') +" class='btn btn-${style}"+(btSize?' btn-'+btSize:'')+(cls?' '+cls.join(' '):'')+"' "+(name?"name='${name}' id='${name}'":'')+(target?' data-target="'+target+'"':'')+(onclick?' onclick="'+onclick+'"':'')+">"
		out << body()
		out << '</button>'
	}
	
	def table = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def type = attrs['type']?attrs.type.split(' '):''
		out << "<table class='table"+(type?' table-' +type.join(' table-'):'')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</table>'
	}
	
	def thumbnail = { attrs ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def src = attrs['src']?attrs.src:''
		def alt = attrs['alt']?attrs.alt:''
		// def width = attrs['width']?attrs.width:''
		// def height = attrs['height']?attrs.height:''
		out << '<img '+(src?'data-src="'+src+'" ':'')+'class="img-thumbnail'+(cls?' '+cls.join(' '):'')+'"'+(alt?' alt="'+alt+'"':'')+" "+(id?" id='${id}'":'')+"/>"
	}	
	
	def label = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def style = attrs['style']?attrs.style:'default'
		out << "<span class='label"+(style?' label-' +style:'')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</span>'
	}
	
	def badge = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:'dropdownMenu1'
		def style = attrs['style']?attrs.style:''
		def title = attrs['title']?attrs.title:''
		def href = attrs['href']?attrs.href:''
		if (href) 
			out << "<a href='${href}' "+(style?' class="btn btn-' +style+'"':'')+""+(id?" id='${id}'":'')+">"
		else
			out << "<button class='btn"+(style?' btn-' +style:'')+"'"+(id?" id='${id}'":'')+">"
		
		out << "${title} <span class='badge"+(cls?' '+cls.join(' '):'')+"'>"
		out << body()
		out << '</span>'
		if (href) 
			out << "</a>"
		else
			out << "</button>"
	}
	def menuButton = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:'dropdownMenu1'
		def style = attrs['style']?attrs.style:'default'
		def title = attrs['title']?attrs.title:''
		def haspopup = attrs['haspopup']?attrs.haspopup:''
		def expanded = attrs['expanded']?attrs.expanded:''
		out << "<div class=\"dropdown\"><button class=\"btn btn-${style}\" type=\"button\" id=\"${id}\" data-toggle=\"dropdown\" "+(haspopup?" aria-haspopup=\"${haspopup}\"":'')+" "+(expanded?" aria-expanded=\"${expanded}\"":'')+">${title} <span class=\"caret\"></span></button><ul class='dropdown-menu"+(cls?' '+cls.join(' '):'')+"' aria-labelledby=\"${id}\">"
		out << body()
		out << '</div></ul>'
	}
	def menu = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def href = attrs['href']?attrs.href:'#'
		def title = attrs['title']?attrs.title:''
		def haspopup = attrs['haspopup']?attrs.haspopup:'true'
		def expanded = attrs['expanded']?attrs.expanded:'false'
		out << "<li class=\"dropdown\""+(id?" id='${id}'":'')+">"
		out << "<a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\" tole=\"button\" "+(haspopup?" aria-haspopup=\"${haspopup}\"":'')+" "+(expanded?" aria-expanded=\"${expanded}\"":'')+">${title} <span class=\"caret\"></span></a>"
		out << "<ul class='dropdown-menu"+(cls?' '+cls.join(' '):'')+"'>"
		out << body()
		out << '</ul>'
		out << '</li>'
	}
	
	/**
	<bs:nav type="pills"></bs:nav>
	type: [pills|tabs]
	justified: [true|false] (optional)
	*/
	def nav = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def type = attrs['type']?attrs.type:'pills'
		def justified = attrs['justified']?attrs.justified:''
		out << "<ul class='nav"+(type?' nav-' +type:'')+(justified=='true'?' nav-justified':'')+(cls?' '+cls.join(' '):'')+"' role=\"tablist\""+(id?" id='${id}'":'')+">"
		out << body() //fill with <li> e.g.: <li class="active" role="presentation"><a href="#" role="tab" data-toggle="tab">Home</a></li>
		out << '</ul>'
	}
	
	def tabContent = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		out << '<div'+(id?" id='${id}'":'')+' class="tab-content'+(cls?' '+cls.join(' '):'')+'">'
		out << body()
		out << '</div>'
	}
	
	def tabPane = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		out << '<div'+(id?" id='${id}'":'')+' class="tab-pane'+(cls?' '+cls.join(' '):'')+'" role="tabpanel">'
		out << body()
		out << '</div>'
	}
	
	/**
	<bs:navbar type="default" brand="Project Name" href="#"></bs:navbar>
	type: [default|inverse]
	fluid: [true|false] (container fluid: optional)
	brand: project name
	href: project URL
	id: id of navbar
	*/
	def navbar = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def type = attrs['type']?attrs.type:'default'
		def fluid = attrs['fluid']?attrs.fluid:''
		def brand = attrs['brand']?attrs.brand:''
		def href = attrs['href']?attrs.href:'#'
		def id = attrs['id']?attrs['id']:'my-nav-bar' // +(Math.round(Math.random()*100)) // if you want to include rand number
		out << "<nav class='navbar navbar-${type}"+(cls?' '+cls.join(' '):'')+"' >"
		out << '<div class="container'+(fluid=='true'?'-fluid':'')+'">'
		out << '<div class="navbar-header">'
		out << '<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#'+id+'" aria-expanded="false">'
		out << '<span class="sr-only">Toggle navigation</span>'
		3.times{out << '<span class="icon-bar"></span>'}
		out << '</button>'
		out << '<a class="navbar-brand" href="'+href+'">'+brand+'</a>'
		out << '</div><!-- .navbar-header -->'
		out << '<div class="collapse navbar-collapse" id="'+id+'">'
		out << body()
		out << '</div><!-- .container'+(fluid=='true'?'-fluid':'')+' -->'
		out << '</nav>'
	}	
	
	/**
	<bs:alert style="success"></bs:alert>
	style: [primary|success|danger|warning]
	dismissible: [true|false] (optional)
	<div class="alert alert-success" role="alert">...</div>
	*/
	def alert = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def style = attrs['style']?attrs.style:''
		def dismissible = attrs['dismissible']?attrs.dismissible:''
		out << "<div class='alert"+(style?' alert-' +style:'')+(dismissible=='true'?' alert-dismissible':'')+(cls?' '+cls.join(' '):'')+"' role=\"alert\""+(id?" id='${id}'":'')+">"
		if (dismissible=='true')
			out << '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
		out << body()
		out << '</div>'
	}
	
	def breadcrumb = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		out << "<ol class='breadcrumb"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</ol>'
	}
	
	def pageHeader = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		out << "<div class='page-header"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</div>'
	}
	
	/**
	<bs:progress value="60" />
	style: [primary|success|danger|warning]
	min: 0
	max:100
	value: 60 (don't include pourcentage sign)
	showValue: [true|false] (if "false" value will not be shown. "true" is default)
	width: explicit css width (otherwise "${value+'%'}" will be used for width)
	striped: [true|false]: (add "progress-bar-striped" class. default: "false")
	*/	
	def progress = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def value = attrs['value']?attrs.value+'%':'0%'
		def min = attrs['min']?attrs.min:'0'
		def max = attrs['max']?attrs.max:'100'
		def showValue = attrs['showValue']?attrs.showValue:'true'
		def style = attrs['style']?attrs.style:''
		def width = attrs['width']?attrs.width:value
		def striped = attrs['striped']?attrs.striped:'false'
		def animated = attrs['animated']?attrs.animated:'false'
		out << "<div class='progress"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << '<div class="progress-bar'+(style?' progress-bar-' +style:'')+(striped=='true'?' progress-bar-striped':'')+'" role="progressbar" aria-valuenow="'+value+'" aria-valuemin="'+min+'" aria-valuemax="'+max+'" style="width: '+width+';">'
		if (showValue == 'false')
			out << '<span class="sr-only">'
		if (body())	
			out << body()
		else
			out << value
		if (showValue == 'false')
			out << '</span>'
		out << '</div>'
		out << '</div><!-- progress -->'
	}
	
	/**
	<bs:media><bs:media positions="left">...</bs:media></bs:media>
	positions: ['left'|'body'|'left body'|...] (optional multiple position only for nested media)
	*/		
	def media = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def positions = attrs['positions']?attrs.positions.split(' '):[]
		out << "<div class='"+(positions?'media-'+positions.join(' media-'):'media')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</div>'
	}
	
	def list = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def tag = attrs['tag']?attrs.tag:'ul'
		out << "<${tag} class='list-group"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</${tag}>'
	}
	
	def item = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def badge = attrs['badge']?attrs.badge:''
		def active = attrs['active']?attrs.active:''
		def disabled = attrs['disabled']?attrs.disabled:''
		def style = attrs['style']?attrs.style:''
		out << "<li class='list-group-item"+(style?' list-group-item-' +style:'')+(active=='true'?' active':'')+(disabled=='true'?' disabled':'')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		if (badge)
			out << '<span class="badge">'+badge+'</span>'
		out << body()
		out << '</li>'
	}
	
	/*
		//Examples
		<panel style="success" id="myPanel" heading="true" title="Title" footer="Total:">
		</panel>
	*/
	def panel = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def heading = attrs['heading']?attrs.heading:'true'
		def style = attrs['style']?attrs.style:'default'
		def title = attrs['title']?attrs.title:''
		def footer = attrs['footer']?attrs.footer:''
		out << "<div class='panel panel-${style}"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		if (heading=='true')
			out << "<div class='panel-heading'>"
		if (title)
			out << "<h3 class='panel-tile'>${title}</h3>"
		if (heading=='true')
			out << '</div>'			
		out << '<div class="panel-body">'
		out << body()
		out << '</div>'
		if (footer)
			out << "<div class='panel-footer'>${footer}</div>"
		out << '</div><!-- .panel -->'
	}
	
	/**
		ratio: [16by9|4by3]
		item: [iframe|video|object|...] (you can set src as well)
		You should set "ratio" or "item" not both
	*/
	def responsive = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def ratio = attrs['ratio']?attrs.ratio:''
		def item = attrs['item']?attrs.item:''
		def src = attrs['src']?attrs.src:''
		out << "<"+(item?'iframe':'div')+" class='"+(ratio?"embed-responsive embed-responsive-${ratio}":'')+(item?'embed-responsive-item':'')+(cls?' '+cls.join(' '):'')+"'"+(src?" src='${raw(src)}'":'')+""+(id?" id='${id}'":'')+">"
		out << body()
		out << '</'+(item?'iframe':'div')+'>'
	}

	def well = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def size = attrs['size']?attrs.size:''
		out << "<div class='well"+(size?' well-' +size:'')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</div>'
	}
	
	def glyphicon = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def icon = attrs['icon']?attrs.icon:''
		def hidden = attrs['hidden']?attrs.hidden:'true'
		out << "<span class='glyphicon"+(icon?' glyphicon-' +icon:'')+(cls?' '+cls.join(' '):'')+"' aria-hidden='"+hidden.toString()+"'"+(id?" id='${id}'":'')+" >"
		out << body()
		out << '</span>'
	}
	
	def row = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		out << "<div class='row"+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</div>'
	}
	
	def col = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def option = attrs['option']?attrs.option:''
		def size = attrs['size']?attrs.size.split(' '):['md-1']
		if (option)
			size = size.collect{ it.replace('-', '-'+option+'-') }
		out << "<div class='"+(size?'col-'+size.join(' col-'):'')+(cls?' '+cls.join(' '):'')+"'"+(id?" id='${id}'":'')+">"
		out << body()
		out << '</div>'
	}
	/**
	width: [auto|fit|300px|50%|...]
	*/
	def select = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def label = attrs['label']?attrs.label:''
		def required = attrs['required']?attrs.required:'false'
		def style = attrs['style']?attrs['style']:''
		def multiple = attrs['multiple']?attrs['multiple']:'false'
		def search = attrs['search']?attrs['search']:'false'
		def max = attrs['max']?attrs.max:''
		def title = attrs['title']?attrs.title:''
		def format = attrs['format']?attrs['format']:''
		def width = attrs['width']?attrs['width']:''
		def size = attrs['size']?attrs['size']:''
		def actions = attrs['actions']?attrs['actions']:'false'
		def header = attrs['header']?attrs['header']:''
		def disabled = attrs['disabled']?attrs['disabled']:'false'
		out << '<div class="form-group">'
		if (label)
			out << "<label class=\"control-label\" for='${attrs.name}'>${label}</label>"
		out << "<select class='selectpicker"+(cls?' '+cls.join(' '):'')+"' name='${attrs.name}' id='${attrs.name}'"+(multiple=='true'?' multiple':'')+(search=='true'?' data-live-search="true"':'')+(max?" data-max-options='${max}'":'')+(title?" title='${title}'":'')+(format?'  data-selected-text-format="'+format+'"':'')+(style?' data-style="btn-'+style+'"':'')+(width?' data-width="'+width+'"':'')+(size?' data-size="'+size+'"':'')+(actions=='true'?' data-actions-box="true"':'')+(header?' data-header="'+header+'"':'')+(disabled=='true'?' disabled':'')+(required=='true'?" required='required'":'')+">"
		out << body()
		out << '</select>'
		out << '</div>'
	}
	
	def inputText = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def label = attrs['label']?attrs.label:''
		def value = attrs['value']?attrs.value:''
		def required = attrs['required']?attrs.required:'false'
		def readonly = attrs['readonly']?attrs.readonly:'false'
		def type = attrs['type']?attrs.type:'text'
		def placeholder = attrs['placeholder']?attrs.placeholder:''
		out << '<div class="form-group">'
		if (label)
			out << "<label class=\"control-label\" for='${attrs.name}'>${label}</label>"
		out << "<input type=\"${type}\" name='${attrs.name}' id='${attrs.name}' class=\"form-control"+(cls?' '+cls.join(' '):'')+"\" "+(value?" value=\"${value}\"":'')+(placeholder?' placeholder="'+placeholder+'"':'')+(required=='true'?" required='required'":'')+(readonly=='true'?" readonly='readonly'":'')+" />"
		out << '</div>'
	}
	def inputPassword = { attrs, body ->
		attrs['type'] = 'password'
		out << grailsApplication.mainContext.getBean('kickstart.BootstrapTagLib').inputText(attrs)
	}
	def inputCheckbox = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def label = attrs['label']?attrs.label:''
		def checked = attrs['checked']?attrs.checked:'false'
		def type = attrs['type']?attrs.type:'checkbox'
		def placeholder = attrs['placeholder']?attrs.placeholder:''
		out << '<div class="form-group">'
		out << "<input type=\"${type}\" name='${attrs.name}' id='${attrs.name}'"+(cls?' class="'+cls.join(' ')+'"':'')+(checked=='true'?" checked='checked'":'')+" />"
		if (label)
			out << " <label class=\"control-label\" for='${attrs.name}'>${label}</label>"
		out << '</div>'	
	}
	
	def inputSubmit = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def value = attrs['value']?attrs.value:''
		def style = attrs['style']?attrs.style:'default'
		out << '<div class="form-group">'
		out << "<input type=\"submit\" name='${attrs.name}' id='${attrs.name}' class=\"btn btn-${style}"+(cls?' '+cls.join(' '):'')+"\" "+(value?" value=\"${value}\"":'')+" />"
		out << '</div>'
	}
	
	def modal = { attrs, body ->
		def cls = attrs['class']?attrs['class'].split(' '):[]
		def id = attrs['id']?attrs['id']:''
		def title = attrs['title']?attrs.title:''
		out << '<div class="modal fade"'+(id?" id='${id}'":'')+' tabindex="-1" role="dialog" aria-labelledby="myModalLabel">'
		out << '<div class="modal-dialog" role="document">'
		out << '<div class="modal-content">'
		out << '<div class="modal-header">'
		out << '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
		out << '<h4 class="modal-title">'+title+'</h4>'
		out << '</div><!-- .modal-header -->'
		out << body()
		out << '</div><!-- .modal-content -->'
		out << '</div><!-- .modal-dialog -->'
		out << '</div><!-- .modal -->'
	}
	
}
