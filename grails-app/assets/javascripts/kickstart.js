if (typeof jQuery !== 'undefined') {
	$(function() {
		/**
		 * Activate Datepicker for Bootstrap
		 */
		$(".datepicker").datepicker({
			/*format:"dd/mm/yyyy",
			language: "fr",*/
			todayHighlight: true,
			autoclose: true
			}).on('show.bs.datepicker', function(e) {
				// if ($(this).data('datepicker').getDate() == null)
					// $(this).datepicker('setDate', e.target.value);
		});	
		
		// Default values for select-picker (some of them can be found in i18n/messages.properties
/*		  $.fn.selectpicker.defaults = {
			noneSelectedText: 'Aucune sélection',
			noneResultsText: 'Aucun résultat pour {0}',
			countSelectedText: function (numSelected, numTotal) {
			  return (numSelected > 1) ? "{0} éléments sélectionnés" : "{0} élément sélectionné";
			},
			maxOptionsText: function (numAll, numGroup) {
			  return [
				(numAll > 1) ? 'Limite atteinte ({n} éléments max)' : 'Limite atteinte ({n} élément max)',
				(numGroup > 1) ? 'Limite du groupe atteinte ({n} éléments max)' : 'Limite du groupe atteinte ({n} élément max)'
			  ];
			},
			multipleSeparator: ', ',
			selectAllText: 'Tout Sélectionner',
			deselectAllText: 'Tout Dé-selectionner',
		  };		
*/		
		/**
		 * Close Dropdown menus when user clicks outside a menu (on the body)
		 */
		$("body").bind("click", function (e) {
			$('.menu').parent("li").removeClass("open");
		});
		
		/**
		 * Toggle Dropdown menus when user clicks on the menu's "switch"
		 */
		// $(".dropdown-toggle, .menu").click(function (e) {
			// var $li = $(this).parent("li").toggleClass('open');
			// return false;
		// });
		
		/**
		 * Close other Dropdown menus that are open when user opens a menu
		 */
	    $('.dropdown-toggle').each(function(){
	        $(this).on("click", function () {
	        	$(this).parent().parent().siblings().each(function(){
	        		$(this).find('.dropdown').removeClass('open');
	        	});
	        });
	    });

	});
}
