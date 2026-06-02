# Self Sign 
## Make sure to keep broker alias as your 
`conatiner host name<kafka.cisco.com>`
Create kafka broker server certificate 
- keytool -keystore kafka.server.keystore.jks -alias localhost -keyalg RSA -validity {validity} -genkey

self sign certificate
- openssl req -new -x509 -keyout ca-key -out ca-cert -days {validity}.

-       This will generate 2 files ca-key and ca-cert 
Create client truststore but not using ca-cert(which is public key) used for importing  client.
-   keytool -keystore kafka.client.truststore.jks -alias CARoot -importcert -file ca-cert
Now import same public key on server truststore
    
-   keytool -keystore kafka.server.truststore.jks -alias CARoot -importcert -file ca-cert

keytool -keystore kafka.server.keystore.jks -alias localhost -certreq -file cert-file

openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days {validity} -CAcreateserial -passin pass:{ca-password}

keytool -keystore kafka.server.keystore.jks -alias CARoot -importcert -file ca-cert

keytool -keystore kafka.server.keystore.jks -alias localhost -importcert -file cert-signed

keytool -exportcert -alias CARoot -keystore kafka.server.keystore.jks -rfc -file CARoot.pem