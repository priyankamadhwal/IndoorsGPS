const express = require('express')
const app = express()

app.use(express.json())

app.put('/updateLocation/:userID', (req, res) => {

    console.log(req.params.userID);
    console.log(req.body);

    res.status(200).send();
})

app.listen(3000, () => {
    console.log("Listening on port 3000...")
})