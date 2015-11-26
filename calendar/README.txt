How to install and use:
1. You have to have Node JS, NPM and MongoDB
2. Create a mongo database named nodewebappdb
3. Run mongod
4. Use the command 'node server.js' (in OS X)
5. Go to your browser and navigate to localhost:3000/
6. To add an event click 'Add Event'
7. From there add a name, start date, end date and description (the date format may depend on your browser; e.g., on firefox you have to type it in yourself in 'MM/DD/YY HH:MM:SS' format, in Chrome it has a selector
8. Either submit or cancel
9. Press edit, to edit the entry. The edit window is similar to the add window. Press Update to update it.
10. Press the Delete button to delete the entry
11. Press Month to view the events by month. From there use the input field to choose the month and year. Press Back to return to the homepage
12. Press Day to view the events by day. From there use the input field to choose the date. Press Back to return to the homepage.


Used these tutorials for help: 
https://www.airpair.com/javascript/complete-expressjs-nodejs-mongodb-crud-skeleton
https://www.youtube.com/watch?v=kHV7gOHvNdk (video series)

The core of the service is made with NodeJs, ExpressJs AngularJs and MongoDB (MEAN). The Google interaction is mainly made
following the Google API tutorials. The libraries used can be found on the first lines of the server.js.
