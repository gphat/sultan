YUI.add('sultan-models', function(Y) {

  // Enable alerts
  $().alert();
  $('#ticket-info').affix();

  Y.ShowAlert = function (aclass, message) {

    // Get the alert area
    var area = $("#alert-area");
    // and find any existing alerts
    var existing = area.find("div.alert");
    // run the template
    var source   = $("#alerter").html();
    var template = Handlebars.compile(source);
    var al = $(template({ alert_class: aclass, message: message }));
    al.hide();

    // Stick the append into a function so we can use it later.
    var appender = function() { al.appendTo(area).slideDown(); }

    // If we have any existing alerts, trash 'em
    if(existing.size() > 0) {
      existing.slideUp("fast", function() {
        existing.remove();
        appender();
      });
    // else, just append it.
    } else {
      appender();
    }
  }

  Y.LinkTicket = Y.Base.create('linkTicket', Y.Model, [Y.ModelSync.REST], {
    root: '/api/linkticket'
  });

  Y.Project = Y.Base.create('project', Y.Model, [Y.ModelSync.REST], {
    root: '/api/project'
  })

  Y.Ticket = Y.Base.create('ticket', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket'
  });

  Y.TicketLink = Y.Base.create('ticketLink', Y.Model, [Y.ModelSync.REST], {
    root: '/api/ticket/link',
    url: '/api/ticket/link/{ticket_id}/{id}'
  });

  Y.UserGroup = Y.Base.create('userGroup', Y.Model, [Y.ModelSync.REST], {
    root: '/api/group',
    url: '/api/group/{id}/{user_id}'
  });
});
