var express = require('express');
var app = express();
var mongojs = require('mongojs');
// events collection in the calendar database
var db = mongojs('calendar', ['events']);
// needed for parsing of json objects
var bodyParser = require('body-parser');

//GoogleAPI https://developers.google.com/google-apps/calendar/quickstart/nodejs
var fs = require('fs');
var readline = require('readline');
var google = require('googleapis');
var googleAuth = require('google-auth-library');

var SCOPES = ['https://www.googleapis.com/auth/calendar'];
var TOKEN_DIR = (process.env.HOME || process.env.HOMEPATH ||
    process.env.USERPROFILE) + '/.credentials/';
var TOKEN_PATH = TOKEN_DIR + 'calendar-nodejs-quickstart.json';
var calendar;
var JsonToken;

// Load client secrets from a local file.
fs.readFile('client_secret.json', function processClientSecrets(err, content) {
  if (err) {
    console.log('Error loading client secret file: ' + err);
    return;
  }
  // Authorize a client with the loaded credentials, then call the
  // Google Calendar API.
  authorize(JSON.parse(content), listEvents);

  // Saving the token for further use
  JsonToken = JSON.parse(content);
});

/**
 * Create an OAuth2 client with the given credentials, and then execute the
 * given callback function.
 *
 * @param {Object} credentials The authorization client credentials.
 * @param {function} callback The callback to call with the authorized client.
 */
function authorize(credentials, callback) {
  var clientSecret = credentials.installed.client_secret;
  var clientId = credentials.installed.client_id;
  var redirectUrl = credentials.installed.redirect_uris[0];
  var auth = new googleAuth();
  var oauth2Client = new auth.OAuth2(clientId, clientSecret, redirectUrl);

//JOS EI AUTHENTICOINTI TOIMI POISTA TÄMÄ KOMMENTTI getNewToken(oauth2Client, callback);

  // Check if we have previously stored a token.
  fs.readFile(TOKEN_PATH, function(err, token) {
    if (err) {
      getNewToken(oauth2Client, callback);
    } else {
      oauth2Client.credentials = JSON.parse(token);
      callback(oauth2Client);
    }
  });
}

/**
 * Get and store new token after prompting for user authorization, and then
 * execute the given callback with the authorized OAuth2 client.
 *
 * @param {google.auth.OAuth2} oauth2Client The OAuth2 client to get token for.
 * @param {getEventsCallback} callback The callback to call with the authorized
 *     client.
 */
function getNewToken(oauth2Client, callback) {
  var authUrl = oauth2Client.generateAuthUrl({
    access_type: 'offline',
    scope: SCOPES
  });
  console.log('Authorize this app by visiting this url: ', authUrl);
  var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  rl.question('Enter the code from that page here: ', function(code) {
    rl.close();
    oauth2Client.getToken(code, function(err, token) {
      if (err) {
        console.log('Error while trying to retrieve access token', err);
        return;
      }
      oauth2Client.credentials = token;
      storeToken(token);
      callback(oauth2Client);
    });
  });
}

/**
 * Store token to disk be used in later program executions.
 *
 * @param {Object} token The token to store to disk.
 */
function storeToken(token) {
  try {
    fs.mkdirSync(TOKEN_DIR);
  } catch (err) {
    if (err.code != 'EEXIST') {
      throw err;
    }
  }
  fs.writeFile(TOKEN_PATH, JSON.stringify(token));
  console.log('Token stored to ' + TOKEN_PATH);
}

// Apufunktio listaamiseen

function listEvents(auth) {
  calendar = google.calendar('v3');
  calendar.events.list({
    auth: auth,
    calendarId: 'primary',
    singleEvents: true,
    orderBy: 'startTime'
  }, function(err, response) {
    if (err) {
      console.log('The API returned an error: ' + err);
      return;
    }
    var events = response.items;
    if (events.length == 0) {
      console.log('No upcoming events found.');
    } else {
      console.log('Upcoming 10 events:');
      for (var i = 0; i < events.length; i++) {
        var event = events[i];
        var start = event.start.dateTime || event.start.date;
        console.log('%s - %s - %s', start, event.summary, event.id);
        console.log("Modified: " + event.updated);
      }
    }
  });
}


// END OF GOOGLE API https://developers.google.com/google-apps/calendar/quickstart/nodejs

// Where all the static files are (e.g. HTML)
app.use(express.static(__dirname + "/public"));
app.use(bodyParser.json());

// GET events
app.get('/events', function(req, res) {
  console.log("Received GET request")

  // Retrieves a list of all events from google calendar
  // Used for checking if events have been added/modified/deleted through google calendar
  authorize(JsonToken, function eventLoop(auth) {

    calendar = google.calendar('v3');
    calendar.events.list({
      auth: auth,
      calendarId: 'primary',
      singleEvents: true,
      orderBy: 'startTime'
    }, function(err, response) {
      if (err) {
        console.log('The API returned an error: ' + err);
        return;
      }
      var events = response.items;
      // Checks if an event has been removed from google calendar
      // Which means we have to delete it from mongo as well
      db.events.find(function(err, docs) {
        var googList = [];
        // Get a list of googIds from google calendar api
        for (var i = 0; i < events.length; i++) {
          var event = events[i];
          googList.push(event.id);
        }

        for (var i = 0; i < docs.length; i++) {
          var doc = docs[i];
          if (googList.indexOf(doc.googId) == -1) {
            db.events.remove({googId: doc.googId}, function(err, doc) {
              console.log("Removed");
            });
          }
        }
      });

        // Checks if an event has been added or modified through google
        for (var i = 0; i < events.length; i++) {
          var event = events[i];
          // Callback function for added events
          (function(i_copy) {
            db.events.find({googId: event.id}, function(err, doc) {
              if (doc.length == 0) {
                var event = events[i_copy];
                var newEvent = {
                  name: event.summary,
                  start: event.start.dateTime,
                  end: event.end.dateTime,
                  description: event.description,
                  updated: event.updated,
                  googId: event.id
                };
                db.events.insert(newEvent, function(err, doc) {
                  console.log("Inserted " + newEvent.name);
                });

              } else {
              }
            });
          }(i));
          // Callback function for updated events
          (function(i_copy) {
            db.events.find({updated: event.updated}, function(err, doc) {
              if (doc.length == 0) {
                var event = events[i_copy];
                var newEvent = {
                  name: event.summary,
                  start: event.start.dateTime,
                  end: event.end.dateTime,
                  description: event.description,
                  updated: event.updated,
                  googId: event.id
                };
                db.events.findAndModify({query: {googId: newEvent.googId},
                    update: {$set: {name: newEvent.name, start: newEvent.start, end: newEvent.end, description: newEvent.description, updated: newEvent.updated}},
                    new: true}, function(err, doc) {
                    });
              }
            });
          }(i));
        };
    });

  });

  // Get all events from mongo
  db.events.find(function(err, docs) {
    // Sends data to the controller
    res.json(docs);
  });
  // FOR TESTING
  authorize(JsonToken, listEvents);

  // Setting the object id to sync the google and mongo

  req.body._id= mongojs.ObjectId(req.params.id);

  console.log(req.body);
});

// Adds new event to calendar
app.post('/events', function(req, res) {

  var bod = req.body;
  console.log(req.body);
  // Tässä googId, menee tämän hetkisen ajan mukaan (millisekunnit ml.)
  bod.updated = null;
  bod.googId = new Date().valueOf().toString();
  // Inserts the event to the events collection
  db.events.insert(bod, function(err, doc) {
    // Sends data to the controller
    res.json(doc);
  });

// Getting the object id to sync the google and mongo
var id = req.body.googId;

// Google api calls by authorize(JsonToken, Function)

  authorize(JsonToken, function addEvent(auth) {

// From here https://developers.google.com/google-apps/calendar/v3/reference/events/insert
    var event = {
  'id': id,
  'summary': req.body.name,
  'location': '',
  'description': req.body.description,
  'start': {
    'dateTime': req.body.start,
    'timeZone': 'Europe/Helsinki',
  },
  'end': {
    'dateTime': req.body.end,
    'timeZone': 'Europe/Helsinki',
  },

  'attendees': [
    {'email': 'mccgroup57@gmail.com'},
  ],
  'reminders': {
    'useDefault': false,
    'overrides': [
      {'method': 'email', 'minutes': 24 * 60},
    ]
  },
};

calendar.events.insert({
  auth: auth,
  calendarId: 'primary',
  resource: event,
}, function(err, event) {
  if (err) {
    console.log('There was an error contacting the Calendar service: ' + err);
    return;
  }
  console.log('Event created: %s', event.htmlLink);
    });

  });
});

// Removes an event from the calendar (by id)
app.delete('/events/:id/:googId', function(req, res) {
  var id = req.params.id;
  var goog = req.params.googId;
  console.log("This is the id: " + req.params.id);
  console.log("This is the googId: " + req.params.googId);

  // Authorizing needed always

  authorize(JsonToken, function deleteEvent(auth) {

    calendar.events.delete({
      //auth needed
      auth: auth,
      calendarId: 'primary',
      eventId: goog
    });
  });

  db.events.remove({_id: mongojs.ObjectId(id)}, function(err, doc) {
    // Sends data to the controller
    res.json(doc);
  });



// From here https://developers.google.com/google-apps/calendar/v3/reference/events/delete


});

// Edit event
app.get('/events/:id', function(req, res) {
  var id = req.params.id;
  db.events.findOne({_id: mongojs.ObjectId(id)}, function(err, doc) {
    // Sends data to the controller
    res.json(doc);
  })
});

// Updates an event
app.put('/events/:id/:googId', function(req, res) {
  var id = req.params.id;
  var goog = req.params.googId;
  console.log("GOOG: " + goog);
  db.events.findAndModify({query: {_id: mongojs.ObjectId(id)},
      update: {$set: {name: req.body.name, start: req.body.start, end: req.body.end, description: req.body.description}},
      new: true}, function(err, doc) {
        res.json(doc);
      });

  authorize(JsonToken, function updateEvent(auth) {
    console.log("GOOGOL: " + goog);
    var event = {
      'id': goog,
      'summary': req.body.name,
      'location': '',
      'description': req.body.description,
      'start': {
        'dateTime': req.body.start,
        'timeZone': 'Europe/Helsinki',
      },
      'end': {
        'dateTime': req.body.end,
        'timeZone': 'Europe/Helsinki',
      },

      'attendees': [
        {'email': 'mccgroup57@gmail.com'},
      ],
      'reminders': {
        'useDefault': false,
        'overrides': [
          {'method': 'email', 'minutes': 24 * 60},
        ]
      },
    };

    calendar.events.update({
      auth: auth,
      calendarId: 'primary',
      resource: event,
      eventId: goog
    }, function(err, event) {
      if (err) {
        console.log('There was an error contacting the Calendar service: ' + err);
        return;
      }
      console.log('Event updated: %s', event.htmlLink);
        });
  });
});

  // GET events for day page
  app.get('/day/:day', function(req, res) {
    var day = new Date(req.params.day);
    var end = new Date(req.params.day);
    end = new Date(end.setTime(day.getTime() + 1 * 86400000));
    db.events.find(function(err, docs) {
      var newDocs = [];
      for (var i = 0; i < docs.length; i++) {
        var doc = docs[i];
        if (new Date(doc.start) >= day && new Date(doc.start) < end) {
          newDocs.push(doc);
        }
      }
      res.json(newDocs);
    });

  });

  // GET events for month page
  app.get('/month/:month', function(req, res) {
    console.log(req.params.month);
    var beginning = new Date(req.params.month);
    var end = new Date(req.params.month);
    end = new Date(end.setTime(end.getTime() + 31 * 86400000));
    db.events.find(function(err, docs) {
      var newDocs = [];
      for (var i = 0; i < docs.length; i++) {
        var doc = docs[i];
        if (new Date(doc.start) >= beginning && new Date(doc.start) < end) {
          newDocs.push(doc);
        }
      }
      res.json(newDocs);
    });
  });
app.listen(3000);
console.log("Server running on port 3000");
