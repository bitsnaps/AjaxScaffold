/**
 * @author bitsnaps
 */

$(function(){

//	var dateFormatIn = 'Y-MM-DD [T] hh:mm:ss [Z]';
//	var dateFormatOut = 'DD/MM/Y';
	
	//init jQuery-BootGrid
	var bootGrid = $('#grid');
	bootGrid.bootgrid({
		ajax: true,
		url: 'json'
		,rowCount: [5,10,15,20,50,-1]
		,multiSort:true
        ,formatters: {
          "commands": function(col, row)  
            {
		        return "<button type=\"button\" onclick=\"editModel(this)\" class=\"btn btn-xs btn-default command-edit\" data-row-id=\""+row.id+"\"><span class=\"glyphicon glyphicon-pencil\"></span></button> " + 
							"<button type=\"button\" onclick=\"deleteModel(this);\" class=\"btn btn-xs btn-default command-delete\" data-row-id=\""+row.id+"\"><span class=\"glyphicon glyphicon-trash\"></span></button>";
					}
        }, //formatters
        converters: {
        	/*/if you want to format date using MomentJS
        	datetime: {
				from: function (value) { return moment(value); },
				to: function (value) { if (value !== null) return moment(value, dateFormatIn).format(dateFormatOut); }
			},
			//add data-converter="boolean" to the grid header (<th>)
        	boolean: {
				from: function(value) { return value; },
				to: function(value) { if (value) {return 'Oui' } else {return 'Non'}}
			}
			//data-converter="object"
			object: {
				from: function(value) {return value;},
				to: function(value){return value.class+'('+value.id+')';}
			}*/
        } //converters
		,labels: { //should be localized
				search: 'Search',
				noResults: 'No results found!'
		} //labels
	});

    $("#grid-header").find('.actions.btn-group').append('<button class="btn btn-default" type="button" onclick="createModel(this)"><span class="icon glyphicon glyphicon-plus"></span></button>');
    			
	$('#modal-form').on('show.bs.modal', function(event){
		$('#form-edit')[0].reset();
		$('input[type=hidden]').val('');
		$('#field-errors').html('');
		//$('input.datepicker').datepicker('update');
		
	}).on('hidden.bs.modal', function(){
		bootGrid.bootgrid('reload');
	});

	$('form[data-async]').submit(function(event) {
			var $form = $(this);
			var $target = $('#modal-form');
			var $action = $form.attr('action');
			//form mode
			var mode =$('#btn-create').is(':visible')?'create':'update';
			var checkboxes = '';
			$form.find('input[type=checkbox]').each(function(){
				if ($(this).prop('checked'))
					$(this).val('true');
				else 
					checkboxes += encodeURIComponent($(this).prop('id'))+"=false&";
			});
			var selectBoxes = '';
			var formData = $form.serialize();
			$form.find('select.selectpicker').each(function(){
				if ($(this).val() == null){
					// formData = formData.replace(new RegExp($(this).prop('id')+"=\\d&", 'g'),'');
					selectBoxes += encodeURIComponent($(this).prop('id'))+"=&";
					// selectBoxes += encodeURIComponent($(this).prop('id'))+"="+encodeURIComponent($(this).val())+"&";
				}
			});
			var serialized = formData +( (checkboxes !== '')?"&"+checkboxes.substr(0,checkboxes.length-1):'' )+
			( (selectBoxes !== '')?"&"+selectBoxes.substr(0,selectBoxes.length-1):'' );
			// console.log(serialized);
			var update = serialized +'&mode='+mode+'&_='+new Date().getTime();
			$.post($action, update).done(function(data){
				if (data == '' || data == null || data.length == 0){
					$target.modal('hide');
				} else {
					// console.log(data.length);
					var errors = $('#field-errors').html('');
					$.each(data, function(i,val){
						errors.append('<li>'+val+'</li>');
					});
				}
			});
			event.preventDefault();
		});

	$('#btn-delete').on("click", function(){
		var row = $(this).attr('data-row-id'); 
		$.post('delete', {id: row}).done(function(data){
			if ($.isNumeric(data)){
				$("#grid").bootgrid('reload');
				$('#id-deleted').text(function(){
					$(this).parent().show();
					$('#delete-error').parent().hide();
					// console.log('data: ' + data);
					return $(this).text().replace(/\[\D?\d+\]/,'['+data+']');
				});
			} else {
				$('#id-deleted').text(function(){
					$(this).parent().hide();
					return $(this).text().replace(/\[\D?\d+\]/,'[-1]');
				});		
				$('#delete-error').text(function(){
					$(this).parent().show();
					return data;
				});
			}
		});                
	});
	
	//format <select> to bootstrap
	$('select').each(function(){
		if (!$(this).hasClass('form-control'))
			$(this).addClass('form-control');
	});
	
}); //$()

function deleteModel(sender){
    var row = $(sender).attr('data-row-id');
    $('#btn-delete').attr('data-row-id', row);
	var deleteMessage = $('#row-id').text();
	$('#row-id').text(deleteMessage.replace(/\{\D?\d+\}/,'{'+row+'}'));
    $('#modal-delete').modal();
}

function createModel(sender){
    //console.log(sender);
    $('#btn-update').hide();
    $('#btn-create').show();
    $('#modal-form').modal();
} //createModel()

function editModel(sender){
    $('#btn-update').show();
    $('#btn-create').hide();
    var row = $(sender).attr('data-row-id');
    if ($('#modal-form').modal()){
        $.getJSON("editJson", {
            edit: row,
            _t:new Date().getTime()
            }, 
            function(data){
            $.each(data, function(k, v){
				// console.log(typeof(v)+':'+k);
				if (typeof(v)=='object' && v!==null){
					var values = $.map(v, function(v,i){return [v];});
					// console.log(v==null);
					// console.log(k+':');
					// console.log(v);
					// console.log('length:' + values.length);
					//many-to-many (selectpicker multiple)
					if (typeof(v.length) !== 'undefined'){
						var selected = [];
						for (var i = 0 ; i < values.length; i++)
							selected.push(values[i]['id'])
						$('#'+k).val(selected);
						// $('#'+k).selectpicker('val', selected);
					} else
						//one-to-many (selectpicker)
						$('#'+k).val(v['id']);
					if ($('#'+k).hasClass('selectpicker'))
						$('#'+k).selectpicker('refresh');
            	} else if ($('#'+k).prop('type') == "checkbox"){
					setCheckbox($('#'+k), v);
					// console.log( $('#'+k) )
            		$('#'+k).prop('checked', v);
            	} else
            		$('#'+k).val(v);
            });
			$('input.datepicker').each(function(){ $(this).datepicker('update')} );
        });
    }
} //editModel()		
