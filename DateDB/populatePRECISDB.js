
//const MongoClient = require('mongodb').MongoClient;
const fs = require('fs');
var xml2js = require('xml2js');
var util = require('util');

const testFolder = './Date PRECIS/';

// // Connection URL
// const url = 'mongodb://localhost:27017';

// // Database Name
// const dbName = 'myDB';

// // Create a new MongoClient
// const client = new MongoClient(url);

// //Promise

// async function main() {
//     try {
//         console.log("inainte");
//         await client.connect();
//         const db = client.db(dbName);
//         const collection = db.collection('myCollection');

//         // const res = await collection.find({
//         //     // "nume": "Sterian"
//         // }).project({_id: 0}).toArray();
//         // res.forEach( element => {
//         //     console.log(element);
//         // })
//         const tolerance = 6;
//         const res = await collection.find({
//             semnal1: {
//                 $gt: 98 - tolerance,
//                 $lt: 98 + tolerance
//             },
//             semnal2: {
//                 $gt: 48 - tolerance,
//                 $lt: 48 + tolerance
//             },
//             semnal3: {
//                 $gt: 55 - tolerance,
//                 $lt: 55 + tolerance
//             }
//         }).toArray();
//         console.log(res);
//         client.close();
//     } catch (error) {
//         console.log(error);
//     }
// }

// main();

var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/";
 
var parser = new xml2js.Parser();

fs.readdir(testFolder, function (err, files) {
    //handling error
    if (err) {
        return console.log('Unable to scan directory: ' + err);
    } 
    //listing all files using forEach
    files.forEach(function (file) {
        // Do whatever you want to do with the file
        //console.log(file);
        
        fs.readFile(testFolder + file, function(err, data) {
            parser.parseString(data, function (err, result) {
                var dataSet = [];
                var wr = result['data']['wr'];
                var loc = result['data']['loc'];
    
                //Loop through JSON obtained from XML file and create a new JSON object with the info i need
                for (var i in loc) {
                    var obj = {};
                    //Latitude and Longitude
                    obj.lat = loc[i]['$'].lat;
                    obj.lng = loc[i]['$'].lng;
    
                    //APs signals
                    var signals = wr[i]['r'];
                    for (r in signals) {
                        obj[wr[i]['r'][r]['$'].b] = parseInt(wr[i]['r'][r]['$'].s, 10);
                    }
    
                    dataSet.push(obj);
                }

                MongoClient.connect(url, function(err, db) {
                    if (err) throw err;
                    var dbo = db.db("myDB");
                    
                    dbo.collection("fingerprints").insertMany(dataSet, function(err, res) {
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
        });
    });
});

