const MongoClient = require('mongodb').MongoClient;

// Connection URL
const url = 'mongodb://localhost:27017';

// Database Name
const dbName = 'myDB';

// Create a new MongoClient
const client = new MongoClient(url);

const fs = require('fs');
const geolib = require('geolib');

// Read input file
let rawdata = fs.readFileSync('./Input Data/input1.txt');  
let fingerprint = JSON.parse(rawdata);

//console.log(query);

async function main() {
    try {
        await client.connect();
        const db = client.db(dbName);
        const collection = db.collection('fingerprints');
        const collectionLIFS = db.collection('fingerprintsLIFS');

        var tolerance = 0;
        var res = [];

        do {
            var query = {};
            for (var attr in fingerprint) {
                //console.log(fingerprint[attr]);
                query[attr] = {
                    $gt: parseInt(fingerprint[attr], 10) - tolerance,
                    $lt: parseInt(fingerprint[attr], 10) + tolerance
                }
            }

            res = await collection.find(query).toArray();
            tolerance++;
        } while(res.length == 0);

        tolerance = 0;
        var resLIFS = [];
        do {
            var query = {};
            for (var attr in fingerprint) {
                //console.log(fingerprint[attr]);
                query[attr] = {
                    $gt: parseInt(fingerprint[attr], 10) - tolerance,
                    $lt: parseInt(fingerprint[attr], 10) + tolerance
                }
            }

            resLIFS = await collectionLIFS.find(query).toArray();
            tolerance++;
        } while(resLIFS.length == 0);

        console.log("Locatia este: lat: " + res[0].lat + ", lng: " + res[0].lng);
        console.log("Locatia conform LIFS este: lat: " + resLIFS[0].lat + ", lng: " + resLIFS[0].lng);
        var dist = geolib.getDistance(
            { latitude: res[0].lat, longitude: res[0].lng},
            { latitude: resLIFS[0].lat, longitude: resLIFS[0].lng },
            0.01
        );

        console.log("Distanta intre cele 2 este: " + dist);

        client.close();
    } catch (error) {
        console.log(error);
    }
}

main();