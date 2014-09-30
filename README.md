diaporama-jsonstore
===================

The JSON store is a very simple server which can store and return JSON documents.

Very Important Notes
--------------------

**This is a test server**, do _not_ use it for real world storage.

- There is currently no security: anyone can store, update and retrieve any document
- There is currently no privacy: the documents are stored uncrypted, so the database admin can read everything

Used Technologies
-----------------

The server is based on the Play framework (2.3.4) and use MongoDB as database.

Installation
------------

### Dev mode

Just run `sbt run` (which starts the server on port 9000) or `sbt "run <PORT>"` to configure the used port.

### Prod mode

- Prepare the binaries with `sbt stage`
- Then copy the `target/universal/stage/` directory on your server
- Run the `target/universal/stage/bin/jsonstore` (or `jsonstore.bat`) on the server

### Heroku

There is already a Procfile, so you can set the configuration variables:

`PORT`
: the port to use

`MONGOHQ_URL`
: the mongo uri to use

Use
---

There are currently 3 available actions

### Storing a new JSON document

Route: `POST /json`
Body: the JSON document to store

This action stores the given document and assigns a new ID to it.

It returns the created document's ID.

### Retrieving a JSON document

Route: `GET  /json/:id`

This action returns the stored document with its ID (merged).

If there is already an `id` field on the JSON document, it will be shadowed by the document's ID.

### Updating an existing JSON document

Route: `POST /json/:id`
Body: the new JSON document

The new JSON document will completely replaces the old one (there is no merge).

### Notes

When using the store and update actions, the request _must_ specify the `Content-Type` header to `application/json` (or `text/json`).

Example
-------

The following example are based on the dev mode with default port.

```bash
# Storing a new JSON document (the server returns the new document's ID)
$ curl http://localhost:9000/json -d '{"a":"b","c":1,"d":true,"e":[{"f":"g"}]}' -H 'Content-type: application/json'
{"id":"542a1f90f718f05700b755bc"}

# Retrieving the document using its ID
$ curl http://localhost:9000/json/542a1f90f718f05700b755bc
{"a":"b","c":1.0,"d":true,"e":[{"f":"g"}],"id":"542a1f90f718f05700b755bc"}

# Updating the document
$ curl http://localhost:9000/json/542a1f90f718f05700b755bc -d '{"h":"i"}' -H 'Content-type: application/json'
{"id":"542a1f90f718f05700b755bc"}

# Retrieving the updated document
$ curl http://localhost:9000/json/542a1f90f718f05700b755bc
{"h":"i","id":"542a1f90f718f05700b755bc"}

# For fun: Retrieving the document directly from the database
$ echo 'db.jsons.find({_id: "542a1f90f718f05700b755bc"})' | mongo jsonstore
MongoDB shell version: 2.6.4
connecting to: jsonstore
{ "_id" : "542a1f90f718f05700b755bc", "json" : { "h" : "i", "id" : "542a1f90f718f05700b755bc" } }
bye
```
