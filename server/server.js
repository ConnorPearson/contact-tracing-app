const http = require('http');
const express = require('express');
const bodyParser = require("body-parser");
const sanitizer = require("string-sanitizer");
const fs = require("fs-extra");

const app = express();
const jsonParser = bodyParser.json();

const exposedUuidsDir = "exposedUuids.json";
const symptomsDir = "symptoms.json";
const additionalsDir = "additionalSymptoms.json";

app.post("/getStatus", jsonParser, async(req, res) => {
        let givenUuid = req.body.uuid;
        let uuidsFromFile = await fs.readJson(exposedUuidsDir);

        if (uuidsFromFile[givenUuid] != null) {
                if ((new Date().getTime() - new Date(uuidsFromFile[givenUuid]).getTime()) > 604800000)  {
                        delete uuidsFromFile[givenUuid];
                }

                console.log("RED : " + givenUuid)
                res.send("RED");
        }
        else {

                console.log("GREEN : " + givenUuid)
                res.send("GREEN");
        }
        
        fs.writeFileSync(exposedUuidsDir, JSON.stringify(uuidsFromFile));
});

app.post("/receiveSymptomaticData", jsonParser, async(req, res) => {
        await appendSymptomsToFile(symptomsDir, req.body);
        await appendAdditionalsToFile(additionalsDir, req.body.additional);

        res.send(true);
});

app.post("/receiveProximityUuids", jsonParser, async(req, res) =>{
        let uuidsFromFile = await fs.readJson(exposedUuidsDir);
        let receviedKeys = Object.keys(req.body);

        for (let i = 0; i < receviedKeys.length; i++) {
                if (uuidsFromFile[receviedKeys[i]] == null) {
                        //Write UUID to JSON with date of exposure
                        uuidsFromFile[receviedKeys[i]] = new Date();
                        console.log("Adding exposed UUID" + receviedKeys[i] + " " + Date());
                }
        }

        fs.writeFileSync(exposedUuidsDir, JSON.stringify(uuidsFromFile));

        res.send(true);
});

async function appendSymptomsToFile(dir, data) {
        let symptomsFromFile = await fs.readJson(dir);

        symptomsFromFile.fever += data.fever;
        symptomsFromFile.fatigue += data.fatigue;
        symptomsFromFile.cough += data.cough;
        symptomsFromFile.lossofappetite += data.lossofappetite;
        symptomsFromFile.bodyache += data.bodyache;
        symptomsFromFile.breathshortness += data.breathshortness;
        symptomsFromFile.mucusorphlegm += data.mucusorphlegm;
        symptomsFromFile.sorethroat += data.sorethroat;
        symptomsFromFile.headaches += data.headaches;
        symptomsFromFile.chillsandshaking += data.chillsandshaking;

        fs.writeFileSync(dir, JSON.stringify(symptomsFromFile));
}

async function appendAdditionalsToFile(dir, data) {
        let additionalsFromFile = await fs.readJson(dir);
        let additionalsJsonSplit = sanitizer.sanitize.keepSpace(data).split(" ");

        for (let i =0; i < additionalsJsonSplit.length && additionalsJsonSplit[0] != ""; i++) {
                additionalsFromFile[additionalsJsonSplit[i].toLowerCase()]++;

                if (isNaN(additionalsFromFile[additionalsJsonSplit[i].toLowerCase()])){
                        additionalsFromFile[additionalsJsonSplit[i].toLowerCase()] = 1;
                }
        }

        fs.writeFileSync(dir, JSON.stringify(additionalsFromFile));
}

app.listen(3000, function(){
        console.log("Listening on port 3000!");
});