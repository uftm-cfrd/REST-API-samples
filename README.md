# FT Lab public REST API usage samples
This project shows some samples for consuming the FT Lab public REST API.
For more information about FT Lab and it's public REST API, please visit: https://admhelp.microfocus.com/digitallab/en/latest/Content/REST_API.HTM

Usage: download the archive, replace the relevant constants in the code and build the code using maven

- SERVER: FT Lab FQDN or IP address
- USER: FT Lab user name
- PASSWORD: FT Lab password
- PROXY: If the connection to the server requires using a proxy, specify proxy address
- APP: full path to the mobile app file (IPA or APK file extensions) to be uploaded to FT Lab

When using *Core Software Delivery Platform (Core SDP) FT Lab tenant*, it is required to use access token instead of user and password. Checkout the CoreSDP_APIClient class for an example.

Please see "main" for the examples of API calls.