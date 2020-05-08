sudo docker exec -ti springandrest_receiver_1 sh /cube/agent/change_to_record.sh
sudo docker exec -ti transformer sh /cube/agent/change_to_record.sh

echo "Enter golden name"
read COLLECTION_NAME



TIMESTAMP=$(date +%s)
RESPONSE="$(curl -X POST \
          https://demo.prod.cubecorp.io/api/cs/start/CubeCorp/SpringbootRestTemplateApp/devcluster/DEFAULT \
          -H 'Content-Type: application/x-www-form-urlencoded' \
          -H 'cache-control: no-cache' \
          -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNZXNoREFnZW50VXNlckBtZXNoZHluYW1pY3MuaW8iLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwidHlwZSI6InBhdCIsImN1c3RvbWVyX2lkIjoxLCJpYXQiOjE1ODI4ODE2MjgsImV4cCI6MTg5ODI0MTYyOH0.P4DAjXyODV8cFPgObaULjAMPg-7xSbUsVJ8Ohp7xTQI' \
          -d "name=$COLLECTION_NAME&userId=demo@meshdynamics.io&label=$TIMESTAMP" )"

echo $RESPONSE


RECORDING_ID=$(echo $RESPONSE | sed 's/^.*"id":"\([^"]*\)".*/\1/')

echo "RECORDING_ID:" $RECORDING_ID
