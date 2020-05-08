sudo docker exec -ti springandrest_receiver_1 sh /cube/agent/change_to_mock.sh
sudo docker exec -ti transformer sh /cube/agent/change_to_mock.sh

echo "Enter golden name"
read GOLDEN_NAME

REPLAY_ENDPOINT=http://35.160.68.101:8080
CUBE_ENDPOINT=https://demo.prod.cubecorp.io
CUSTOMERID=CubeCorp
APP=SpringbootRestTemplateApp
INSTANCE_ID=devcluster
USER_ID=demo@cubecorp.io
TEMPLATE=DEFAULT
AUTH_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNZXNoREFnZW50VXNlckBtZXNoZHluYW1pY3MuaW8iLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwidHlwZSI6InBhdCIsImN1c3RvbWVyX2lkIjoxLCJpYXQiOjE1ODI4ODE2MjgsImV4cCI6MTg5ODI0MTYyOH0.P4DAjXyODV8cFPgObaULjAMPg-7xSbUsVJ8Ohp7xTQI"

BODY="endPoint=$REPLAY_ENDPOINT&analyze=true&instanceId=$INSTANCE_ID&templateSetVer=$TEMPLATE&userId=$USER_ID"
	echo $BODY
	resp=$(curl -sw "%{http_code}" -X POST \
		$CUBE_ENDPOINT/api/rs/start/byGoldenName/$CUSTOMERID/$APP/$GOLDEN_NAME \
		-H 'Content-Type: application/x-www-form-urlencoded' \
		-H "Authorization: Bearer $AUTH_TOKEN" \
		-H 'cache-control: no-cache' \
		-d $BODY)
	http_code="${resp:${#res}-3}"
	if [ $http_code -ne 200 ]; then
		echo "Error"
		exit 1
	fi
	body="${resp:0:${#resp}-3}"
	REPLAY_ID=$(echo $body | jq -r ".replayId" | sed -e 's/ /%20/g')

	echo "REPLAYID:" $REPLAY_ID
	#Status Check
	COUNT=0
	while [ "$STATUS" != "Completed" ] && [ "$STATUS" != "Error" ]; do
		STATUS=$(curl -f -X GET $CUBE_ENDPOINT/api/rs/status/$REPLAY_ID -H "Authorization: Bearer $AUTH_TOKEN" | jq -r '.status')
		sleep 10
		COUNT=$((COUNT+1))
	done

	COUNT=0
	while [ "$STATUS" != "Completed" ] && [ "$STATUS" != "Error" ]; do
		STATUS=$(curl -X GET $CUBE_ENDPOINT/api/as/status/$REPLAY_ID -H 'Content-Type: application/x-www-form-urlencoded' -H "Authorization: Bearer $AUTH_TOKEN" -H 'cache-control: no-cache' | jq '.data.status' | tr -d '"')
		sleep 10
		COUNT=$((COUNT+1))
	done
	unset STATUS
	ANALYZE=$(curl -X GET $CUBE_ENDPOINT/api/as/status/$REPLAY_ID -H 'Content-Type: application/x-www-form-urlencoded' -H "Authorization: Bearer $AUTH_TOKEN" -H 'cache-control: no-cache' | jq '.data')
	echo $ANALYZE | jq

sudo docker exec -ti springandrest_receiver_1 sh /cube/agent/change_to_normal.sh
sudo docker exec -ti transformer sh /cube/agent/change_to_normal.sh
