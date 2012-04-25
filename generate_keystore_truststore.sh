#!/bin/sh

$KEYSTORE="./Common/keystore"
$TRUSTSTORE="./Common/truststore"

echo "Generating key"
keytool -genkey -alias manager -keyalg RSA -validity 360 -keystore $KEYSTORE
echo "Listing key"
keytool -list -v -keystore $KEYSTORE

echo "Exporting certificate"
keytool -export -alias manager -keystore $KEYSTORE -rfc -file manager.cer
echo "Importing certificate into truststore"
keytool -import -alias manager -keyalg RSA -file manager.cer -keystore $TRUSTSTORE 
