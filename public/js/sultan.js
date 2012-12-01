var durationParser = function() {
  var durField = $("#duration");
  durField.val(juration.parse(durField.val()));
  return true;
}

function Change(data) {
  this.id           = ko.observable(data.id);
  this.userId       = ko.observable(data.userId);
  this.userRealName = ko.observable(data.userRealName);
  this.ownerId      = ko.observable(data.ownerId);
  this.ownerRealName = ko.observable(data.userRealName);
  this.changeTypeId = ko.observable(data.changeTypeId);
  this.changeTypeName = ko.observable(data.changeTypeName);
  this.changeTypeNameI18N = ko.observable(data.changeTypeNameI18N);
  this.changeTypeColor = ko.observable(data.changeTypeColor);
  this.statusName   = ko.observable(data.statusName);
  this.statusNameI18N = ko.observable(data.statusNameI18N);
  this.statusColor  = ko.observable(data.statusColor);
  this.duration     = ko.observable(data.duration);
  this.risk         = ko.observable(data.risk);
  this.summary      = ko.observable(data.summary);
  this.description  = ko.observable(data.description);
  this.notes        = ko.observable(data.notes);
  this.dateBegun    = ko.observable(data.dateBegun);
  this.begun        = ko.observable(data.begun);
  this.dateClosed   = ko.observable(data.dateClosed);
  this.closed       = ko.observable(data.closed);
  this.dateCompleted= ko.observable(data.dateCompleted);
  this.completed    = ko.observable(data.completed);
  this.dateCreated  = ko.observable(data.dateCreated);
  this.dateScheduled= ko.observable(data.dateScheduled);
  this.dateCreated = ko.observable(data.dateCreated);
}

function ChangeAddViewModel() {
  var self = this;

  self.parseDuration = durationParser;
}

function ChangeEditViewModel() {
  var self = this;

  self.parseDuration = durationParser;
}

function ChangeListViewModel() {
  var self = this;
  self.links = ko.observableArray([]);
}