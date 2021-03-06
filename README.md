# Redis Client

A Redis client to demonstrate working with the API

## Set Up The Database

See the article [Some Redis Notes](https://hindmasj.github.io/misc/redis.html) where I show how to create a Redis server in Docker.

## Run The Demo

Run the script ``bin/demo.sh`` to run the client in demo mode. This connects to the docker server and runs some commands to create, retrieve and delete data.

## Change The Password

The docker article uses the environment variable *REDIS_PASSWORD* to set the password for the database, and uses the unimaginative value of "password". If you want to change this then you need to get the demo to match it.

Run the script ``bin/encrypt.sh`` to run the encryption procedure on your chosen password. Then change the value in "src/main/resources/reference.conf" and repackage. Then you can run the demo again.

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

The encrypted password is stored in the configuration file and is then decrypted by the client just before it is presented to the server. See the note above about encrypting the password with the ``bin/encrypt.sh`` script.

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

To parse a file the input file is specific in the config file under the key "files.geoipv4.source".

```
files{
  geoipv4{
    source=data/sample-geo-data.json
  }
}
```

Run the script ``bin/parse.sh <bulkfile>`` to parse this file into the specific output file. Then use the command in the next section to upload it.

TODO: allow the user to specify the input file on the command line.

## Uploading

The output of the parsing can then be uploaded directly to the database using the redis-cli command. (Note there is no "-t" in the docker command, as you might usually type.)

```
docker exec -i redis redis-cli --pass <password> --pipe < <bulkfile>
```

## Subnet Links

The parse feature adds a number of link records, so that for every IPv4 address covered by the subnet, there can be a link record such that the key is the IPv4 address, and the value is the subnet CIDR, prefixed with a "#". This file is automatically created when you run "parse.sh" and produces a file called *links-&lt;bulkfile&gt;*.

Upload this file to the database in the same way as above.

Then you can put this together with a 2 step search to find the GeoIP information for a test address such as "111.102.105.112". First form a "/32" search key from the address. Then, if the response is a string beginning with "#" perform a new search using the provided link.

```
get geoipv4.111.102.105.112/32
$18
#111.102.105.93/24
get geoipv4.111.102.105.93/24
$144
{"ipv4":"111.102.105.93/24","country":"Indonesia","city":"Krajan Jamprong","provider":"Beatty and Sons","latitude":-6.9604,"longitude":111.6157}
```

If there is a "/32" entry then it will be returned without needing to use the link. Try "172.78.81.101".

```
get geoipv4.172.78.81.101/32
$142
{"ipv4":"172.78.81.101/32","country":"China","city":"Damu","provider":"Barrows, Lang and Leannon","latitude":24.134518,"longitude":111.485304}
```

## Search Demo

If you have done all that you can run the command ``bin/search.sh`` to perform a demo search on some example addresses.

# Standard Files

There are some scripts to turn the popular files */etc/services* and */etc/protocols* into indices.

Run ``bin/staticParse.sh`` to create *services.output* and *protocols.output*. Load these into the server using the CLI pipe, then you can query them.

```
> get service.80/tcp
"{\"name\":\"http\",\"code\":80,\"protocol\":\"tcp\",\"alias\":\"www, www-http\",\"comment\":\"WorldWideWeb HTTP\"}"

> get protocol.17
"{\"name\":\"udp\",\"code\":17,\"alias\":\"UDP\",\"comment\":\"user datagram protocol\"}"
```
