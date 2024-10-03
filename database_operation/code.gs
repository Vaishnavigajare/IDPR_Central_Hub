 var sheetName = 'Sheet1';
  var scriptProp = PropertiesService.getScriptProperties();

function initialSetup() {
  var activeSpreadsheet = SpreadsheetApp.getActiveSpreadsheet();
  scriptProp.setProperty('key', activeSpreadsheet.getId());
}

function doPost(e) {
  var lock = LockService.getScriptLock();
  
  // Try to acquire lock, and proceed only if successful
  if (!lock.tryLock(10000)) {
    return ContentService
      .createTextOutput(JSON.stringify({ 'result': 'error', 'error': 'Could not acquire lock' }))
      .setMimeType(ContentService.MimeType.JSON);
  }
  
  try {
    var doc = SpreadsheetApp.openById(scriptProp.getProperty('key'));
    var sheet = doc.getSheetByName(sheetName);

    // Get headers
    var headers = sheet.getRange(1, 1, 1, sheet.getLastColumn()).getValues()[0];
    var nextRow = sheet.getLastRow() + 1;

    // Create a new row with incoming data
    var newRow = headers.map(function(header) {
      return header === 'timestamp' ? new Date() : e.parameter[header] || '';
    });

    // Check if newRow has proper values, and write to sheet
    sheet.getRange(nextRow, 1, 1, newRow.length).setValues([newRow]);

    return ContentService
      .createTextOutput(JSON.stringify({ 'result': 'success', 'row': nextRow }))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (error) {
    // Log error and return it in response
    Logger.log(error.toString());
    return ContentService
      .createTextOutput(JSON.stringify({ 'result': 'error', 'error': error.toString() }))
      .setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}