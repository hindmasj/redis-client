# Redis Client

A Redis client to demonstrate working with the API

## Set Up The Database

See the article [Some Redis Notes](https://hindmasj.github.io/misc/redis.html) where I show how to create a Redis server in Docker.

## Run The Demo

Run the script ``demo.sh`` to run the client in demo mode. This connects to the docker server and runs some commands to create, retrieve and delete data.

## Change The Password

The docker article uses the environment variable *REDIS_PASSWORD* to set the password for the database, and uses the unimaginative value of "password". If you want to change this then you need to get the demo to match it.

Run the script ``encrypt.sh`` to run the encryption procedure on your chosen password. Then change the value in "src/main/resources/reference.conf" and repackage. Then you can run the demo again.

TODO: Allow the use of a local "application.conf" to alter settings.

# Sample Data

## GeoIPv4

Use [Mockaroo](https://www.mockaroo.com/) with the schema *schemas/SimpleGeoIP.schema.json*. There is no scope in the Mockaroo schema to limit the range of CIDRs, so normalise the CIDR data to prevent ranges getting too large. Minimum range we want is "/16". Convert anything *<10* to "/24" and anything *>=10, <16* to 28.

```
sed -i s%/[[:digit:]]'"'%/24'"'% sample-geo-data.json
sed -i s%/1[0-5]'"'%/28'"'% sample-geo-data.json
```

# Encryption

The password as stored in the configuration file in key "auth.password" is encrypted to avoid casual copying. This uses [jasypt](http://www.jasypt.org/) and their *StandardPBEStringEncryptor* class. This requires a simple cipher key to be used for uniqueness and repeatability. The key is stored in *reference.conf* under the key "auth.ckey", so while it exists in the source code it can be changed without recompiling (by updating the file in the JAR) but does not get seen directly in the file system.

The encrypted password is stored in the configuration file and is then decrypted by the client just before it is presented to the server. See the note above about encrypting the password with the ``encrypt.sh`` script.

# File uploading

## Parsing

The goal of file parsing is to turn an input file, such as the mock GeoIPv4 file, into a file suitable for bulk uploading into the database. See [Redis Mass Insertion](https://redis.io/topics/mass-insert).

Here the input file is a JSON array, and the requirement is to have each JSON record stored as a string, with the key being the value of the first element in the record, prefixed by something to make it domain specific. So in this case a key might be "geoipv4.111.102.105.93/24", and the record would be

``{"ipv4":"111.102.105.93/24","country":"Indonesia","city":"Krajan Jamprong","provider":"Beatty and Sons","latitude":-6.9604,"longitude":111.6157}``

The output of the file parsing then would be a file of Redis bulk loading commands, doing a "SET" for each key-record pair. So something like

```
*3\r\n
$3\r\n
SET\r\n
$26\r\n
geoipv4.111.102.105.93/24\r\n
$145\r\n
{"ipv4":"111.102.105.93/24","country":"Indonesia","city":"Krajan Jamprong","provider":"Beatty and Sons","latitude":-6.9604,"longitude":111.6157}\r\n
*3\r\n
...
```

etc.

## Uploading

The output of the parsing can then be uploaded directly to the database using the redis-cli command.

```
docker exec -it redis redis-cli --pass <password> --pipe < <bulkfile>
```
