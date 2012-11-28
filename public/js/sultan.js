var durationParser = function() {
  var durField = $("#duration");
  durField.val(juration.parse(durField.val()));
  return true;
}

function ChangeAddViewModel() {
  var self = this;

  self.parseDuration = durationParser;
}

function ChangeEditViewModel() {
  var self = this;

  self.parseDuration = durationParser;
}