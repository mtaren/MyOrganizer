
function echo(){
  console.log("eh");
}


/*****************************************************************/
/******************* Utility functions ***************************/
/*****************************************************************/

function AddOnClick(id,fn){
  $(id).click(fn);
}

//doesnt deal with the picture
//todo prevent illegal inputs
function GetParamsFromAddPopup(formType){
  params = {};
  params['Name'] = $('#namePop-' + formType).val();
  params['Desc'] = $('#descPop-' + formType).val();
  params['Category'] = $('#catPop-'  + formType).val();

  //item specific values
  if ((formType=="item") || (formType =="modify-object")){
    params['Qty'] = $('#qtyPop-'  + formType).val();
  }
  
  return params;
}
  
function isPictureChosen(objectType){
  // path = $('#fileupload'+objectType).val();
  if($('#fileupload'+objectType+'Submit').length){
    return true;
  }
  return false;
  // console.log(path);
  // if(path==""){
  //   return false;
  // }
  // return true;
}

/*****************************************************************/
/******************* Init functions ******************************/
/*****************************************************************/
function uninit_Piczoom(){
  if(viewType !="view"){
    return;
  }
   $( ".imgs" ).each(function( index) {
    $(this).unwrap();
 });

}

//init piczoom- call this after adding all pics
function init_PicZoom(){
  if(viewType =="view"){
    return;
  }
  if(viewType =="sel"){
    uninit_SelClicks();
  }
  viewType = 'view';
  $('.grid_item, .grid_container').unbind( "click" );

  $( ".imgs" ).each(function( index) {
    Mysrc =  $(this).attr('src');
    //asdfasdf
    zoom =$('<a/>', {
      class : "image-popup-no-margins",
      href: Mysrc,
      text: name
    });
    $(this).wrap(zoom);
 });

$('.image-popup-no-margins').magnificPopup({
          type: 'image',
          closeOnContentClick: true,
          closeBtnInside: false,
          fixedContentPos: true,
          //disableOn: 100099, turn off zoom? cant really.. have to redraw on stuff
          mainClass: 'mfp-no-margins mfp-with-zoom', // class to remove default margin from left and right side
          image: {
            verticalFit: true
          },
          zoom: {
            enabled: true,
            duration: 300 // don't foget to change the duration also in CSS
          }
        });
}

//init hover effects. Call when items are in gridview
function init_HoverEffects(){  
   $('.grid_item, .grid_container').mouseover(function(){
      $(this).addClass('hover-effect');
   });        
   $('.grid_item, .grid_container').mouseout(function(){
      $(this).removeClass('hover-effect');
   });

}

function init_NavClicks(){  
  //clicking containers
  if(viewType =="sel"){
    uninit_SelClicks();
  }
  if(viewType =="view"){
    uninit_Piczoom();
  }
  $('.grid_item, .grid_container').unbind( "click" );
  //clicking containers
   $('.grid_container').click(function(){
    var contentPanelId = $(this).attr("id");
      clickedContainer(contentPanelId);
   });  

   //clicking items brings up modify
   $('.grid_item').click(function(){
     ItemList = [$(this).attr("id")];
     params['ItemIdList'] = JSON.stringify(ItemList);
     params['ContainerIDList'] = JSON.stringify([]);
     $.post('/WS_GetObjectAttributes',params,function(data,Status){
       data = JSON.parse(data);
       StartModify(data.ContainerList, data.ItemList)
      });
   });  

   //clicking path labels
   $('.path-label').click(function(){
    var contentPanelId = $(this).attr("id");
    if(contentPanelId == "Null"){
      deleteObjs();
      BackStack.push(CurrentContainerId);
      CurrentContainerId = "Null";
      GetHouses_request();
      return
    }
    clickedContainer(contentPanelId);
   });
   viewType = "nav"
}

function init_SelClicks(){  
  if(viewType == "sel"){
    return;
  }
  if(viewType =="view"){
    uninit_Piczoom();
  }
  
  $('.grid_item, .grid_container, .path-label').unbind( "click" );
  //clicking containers
   $('.grid_item, .grid_container').mousedown(function(){
    if( $(this).hasClass('selected') ){
      $(this).removeClass('selected');
    }else{
      $(this).addClass('selected');
    }
  });
  $('.grid_item, .grid_container').dblclick( function(){
      $('.grid_item, .grid_container').addClass('selected');
  });
  $("#SelectBar").fadeIn("fast");
  viewType = "sel"
}

function uninit_SelClicks(){
  $('.selected').removeClass('selected');
  $('.grid_item, .grid_container').unbind( "mousedown" );
  $('.grid_item, .grid_container').unbind( "dblclick" );
  $("#SelectBar").fadeOut();
}

//one init to call after filling grid
function init_fillGrid(viewType){
  if(viewType == "nav"){
    init_NavClicks();
  }
  if(viewType == "view"){
    init_PicZoom();
  }
  if(viewType == "sel"){
    init_SelClicks();

  }
    init_HoverEffects();
}


//initializes an inline popup
function init_PopUp(ID, URL,CLASS){
  // load add container form
    $(ID).load(URL, function(){
  // init popup
    $(CLASS).magnificPopup({
      type: 'inline',
      preloader: false,
      focus: '#name',
      // When elemened is focused, some mobile browsers in some cases zoom in
      // It looks not nice, so we disable it:
      callbacks: {
        beforeOpen: function() {
          if($(window).width() < 700) {
            this.st.focus = false;
          } else {
            this.st.focus = ('.focusme');
          }
        },
        open: function() {
          $('.focusme').select();
          //console.log("opened")
          // Will fire when this exact popup is opened
          // this - is Magnific Popup object
        },
        close: function() {
          //console.log("closed")
          // Will fire when popup is closed
        }
          // e.t.c.
    }
    
    });
  }); 
}

//called from a .change
//input is "this"
//idofImage is one to change the src
function init_Preview_Image(input, idOfImage) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            
            reader.onload = function (e) {
                $(idOfImage).attr('src', e.target.result);
            }
            
            reader.readAsDataURL(input.files[0]);
        }
    }