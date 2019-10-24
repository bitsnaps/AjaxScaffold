<% import grails.persistence.Event %>
<%=packageName%>
<!DOCTYPE html>
<html>

<head>
	<meta name="layout" content="kickstart" />
	<g:set var="entityName" value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
	<title><g:message code="default.index.label" args="[entityName]" /></title>
	<asset:stylesheet src="jquery.bootgrid.min.css" />
	
	<asset:javascript src="jquery.bootgrid.min.js" />
	<asset:javascript src="bootcrud.js" />
	
</head>

<body>

<section id="index-${domainClass.propertyName}" class="first">
			<table id="grid" class="table table-condensed table-hover table-striped" 
				data-selection="true" data-multi-select="false">
				<thead>
					<tr>
					<%  
						excludedProps = Event.allEvents.toList() << 'version'
						allowedNames = domainClass.properties*.name << 'dateCreated' << 'lastUpdated'
	
						props = domainClass.properties.findAll {
							allowedNames.contains(it.name) && 
							!excludedProps.contains(it.name) && 
							it.type != null && 
							!Collection.isAssignableFrom(it.type) && 
							(domainClass.constrainedProperties[it.name] ? domainClass.constrainedProperties[it.name].display : true) 
						}
						
						Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
						
						def pk = props[0]
						
						props.eachWithIndex { p, i ->
							if (i < 8) {
							if (p.isAssociation()) { %>
								<th data-column-id="${p.name}"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></th>
							<%   } else { %>
						
							<th data-column-id="${p.name}"<%=(pk.name==p.name)?' data-identifier="true" data-order="asc"':'' %><%= (p.type==Boolean||p.type==boolean)?' data-converter="boolean"':''%><%=(p.type == Date || p.type == java.sql.Date || p.type == java.sql.Time || p.type == Calendar)?' data-converter="datetime"':''  %>>${p.naturalName}</th>
						<%  }  }   } %>
						<th data-column-id="commands" data-formatter="commands" data-sortable="false">Action</th>
						</tr>
				</thead>
				<tbody></tbody>
				<tfoot></tfoot>
			</table>

			<div class="row col-md-6">
				<div class="alert alert-success alert-dismissible" style="display:none">
					<button type="button" class="close" data-dismiss="alert" aria-label="\${message(code:'modal.button.close', default:'Close')}"><span aria-hidden="true">&times;</span></button>
					<p id="id-deleted"><g:message code="default.deleted.message" default="Deleted" args="[entityName,[-1]]" /></p>
				</div>
				<div class="alert alert-danger alert-dismissible" style="display:none">
					<button type="button" class="close" data-dismiss="alert" aria-label="\${message(code:'modal.button.close', default:'Close')}"><span aria-hidden="true">&times;</span></button>
					<p id="delete-error"></p>
				</div>
			</div><!-- .row col-md-6 --!>
			
        <!-- modal-form -->
        <div class="modal fade" id="modal-form" role="dialog">
           <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="\${message(code:'modal.button.close', default:'Close')}"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">Edit ${className}</h4>
                    </div><!-- .modal-header -->
					
						<g:form name="form-edit" action="updateJson" class="form-vertical" role="form" <%= multiPart ? ' enctype="multipart/form-data"' : '' %>  data-async="data-async">
							<div class="modal-body">
							<g:hiddenField name="<%= pk.name %>" />
							<g:render template="form"/>
							<div class="form-actions margin-top-medium">
								<button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
							</div>
							
							</div><!-- .modal-body -->
                    <div class="modal-footer">
						<div class="pull-left"><ul class="small text-danger" id="field-errors" style="text-align:left;"></ul></div>
                        <button type="button" class="btn btn-default" data-dismiss="modal" id="btn-close">\${message(code:'modal.button.close', default:'Close')}</button>
						<g:submitButton class="btn btn-primary" id="btn-update" name="btn-update" value="\${message(code: 'default.button.update.label', default: 'Update')}" />
						<g:submitButton class="btn btn-success" id="btn-create" name="btn-create" style="display: none" value="\${message(code: 'default.button.create.label', default: 'Create')}" />
						<g:hiddenField name="hdn-inpt" value="hdn_<%= Math.round(Math.random()*1000) %>" />
                    </div>
					</g:form>	
                </div><!--.modal-content-->
            </div><!--.modal-dialog-->
         </div><!--.modal-->

        <!-- Modal Delete -->
        <div class="modal fade" id="modal-delete" tabindex="-1" role="dialog" aria-labelledby="\${message(code: 'default.button.delete.label', default: 'Delete')} ${className}">
          <div class="modal-dialog" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="\${message(code:'modal.button.close', default:'Close')}"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">\${message(code: 'default.button.delete.label', default: 'Delete')} ${className}</h4>
              </div>
              <div class="modal-body">
                  <p id="row-id">\${message(code: 'default.button.delete.confirm.message', default: 'Do you really want to delete ')} </p>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">\${message(code: 'default.button.cancel.label', default: 'Cancel')}</button>
                <button id="btn-delete" type="button" class="btn btn-danger" data-dismiss="modal">\${message(code: 'default.button.delete.label', default: 'Delete')}</button>
              </div>
            </div>
          </div>
        </div><!-- Modal Delete -->
</section>

</body>

</html>
