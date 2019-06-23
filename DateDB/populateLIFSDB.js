const fs = require('fs');
var util = require('util');

const file = './Date LIFS/LIFS.json';

var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/";
    
fs.readFile(file, function(err, data) {    

    if (err) throw err;
    let dataSet = JSON.parse(data);

    MongoClient.connect(url, function(err, db) {
        if (err) throw err;
        var dbo = db.db("myDB");
        
        dbo.collection("fingerprintsLIFS").insertMany(dataSet, function(err, res) {
            if (err) throw err;
            console.log("Number of documents inserted: " + res.insertedCount);
            db.close();
        });
    });

        //console.log(dataSet.length);
        
        /*fs.writeFile('data.json', JSON.stringify(dataSet), function(err, data) {

        });*/
        //console.log('Done');
});