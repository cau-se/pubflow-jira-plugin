(function ($) { 
    var url = AJS.contextPath() + "/rest/pubflow-admin/1.0/";

    $(document).ready(function() {
        $.ajax({
            url: url,
            dataType: "json"
        }).done(function(config) { 
            $("#homedir").val(config.homedir);
        });
    });

})(AJS.$ || jQuery);

function updateConfig() {
  AJS.$.ajax({
    url: baseUrl + "/rest/pubflow-admin/1.0/",
    type: "PUT",
    contentType: "application/json",
    data: '{ "name": "' + AJS.$("#name").attr("value") + '", "time": ' +  AJS.$("#time").attr("value") + ' }',
    processData: false
  });
}

AJS.$("#admin").submit(function(e) {
        e.preventDefault();
        updateConfig();
    });
    
    