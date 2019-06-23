const fs = require('fs');
var xml2js = require('xml2js');
var util = require('util');
const geolib = require('geolib');



var MongoClient = require('mongodb').MongoClient;

var url = "mongodb://localhost:27017/";

// Database Name
const dbName = 'myDB';

// Create a new MongoClient
const client = new MongoClient(url);
 
var parser = new xml2js.Parser();

fs.readFile('./Input Data/path.xml', function(err, data) {
    parser.parseString(data, function (err, result) {
        var dataSet = [];
        var locations = [];
        var wr = result['data']['wr'];
        var loc = result['data']['loc'];

        //Loop through JSON obtained from XML file and create a new JSON object with the info i need
        for (var i in loc) {
            var obj = {};
            var l = {};

            //Latitude and Longitude
            l.lat = loc[i]['$'].lat;
            l.lng = loc[i]['$'].lng;

            //APs signals
            var signals = wr[i]['r'];
            for (r in signals) {
                obj[wr[i]['r'][r]['$'].b] = parseInt(wr[i]['r'][r]['$'].s, 10);
            }

            dataSet.push(obj);
            locations.push(l);
            //console.log(dataSet);
        }

        fs.writeFile('./Input Data/input3.txt', JSON.stringify(dataSet), function (err) {
            if (err) {
                console.error('Crap happens');
            }
        });

        fs.writeFile('./Input Data/locations_input3.txt', JSON.stringify(locations), function (err) {
            if (err) {
                console.error('Crap happens');
            }
        });
    });
});

// Read input file
let rawdata = fs.readFileSync('./Input Data/input3.txt');  
let fingerprint = JSON.parse(rawdata);

let rawdata1 = fs.readFileSync('./Input Data/locations_input3.txt');
let locations = JSON.parse(rawdata1);

async function main() {
    try {
        await client.connect();
        const db = client.db(dbName);
        const collection = db.collection('fingerprintsLIFS');
        var res = [];
        var distances = [];
        var aux;
        console.log(aux);

        for (var i in fingerprint) {
            var tolerance = 0;

            do {
                var query = {};
                for (var attr in fingerprint[i]) {
                    //console.log(fingerprint[attr]);
                    query[attr] = {
                        $gt: parseInt(fingerprint[i][attr], 10) - tolerance,
                        $lt: parseInt(fingerprint[i][attr], 10) + tolerance
                    }
                }

                res[i] = await collection.find(query).toArray();
                tolerance++;
                if (tolerance > 70) {
                    res[i] = res[i-1];
                }
            } while(res[i].length == 0);

            var j = 0;
            var minDist = 999;
            for (var x in res[i]) {
                if (aux == null) {
                    break;
                }
                var dist = geolib.getDistance(
                    { latitude: res[i][x].lat, longitude: res[i][x].lng},
                    { latitude: aux.lat, longitude: aux.lng },
                    0.01
                );
                if (dist < minDist) {
                    j = x;
                    minDist = dist;
                }
            }

            console.log("Locatia " + i + " este: lat: " + res[i][j].lat + ", lng: " + res[i][j].lng);

            aux = res[i][j];

            var dist = geolib.getDistance(
                { latitude: res[i][j].lat, longitude: res[i][j].lng},
                { latitude: locations[i].lat, longitude: locations[i].lng },
                0.01
            );
    
            console.log("Distanta intre cele doua este: " + dist);
            distances.push(dist);
        }

        var sum = 0;
        for (var i in distances) {
            if (distances[i] < 20)
                sum += distances[i];
        }
        var avg = sum/distances.length;
        console.log("Media erorilor este: ", avg);

        client.close();
    } catch (error) {
        console.log(error);
    }
}

main();