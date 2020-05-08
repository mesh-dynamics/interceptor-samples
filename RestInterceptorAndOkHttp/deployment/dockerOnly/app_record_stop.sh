sudo docker exec -ti springandrest_receiver_1 sh /cube/agent/change_to_normal.sh
sudo docker exec -ti transformer sh /cube/agent/change_to_normal.sh

echo "Enter the Recording Id"
read RECORDING_ID

 

curl -X POST https://demo.prod.cubecorp.io/api/cs/stop/$RECORDING_ID -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNZXNoREFnZW50VXNlckBtZXNoZHluYW1pY3MuaW8iLCJyb2xlcyI6WyJST0xFX1VTRVIiXSwidHlwZSI6InBhdCIsImN1c3RvbWVyX2lkIjoxLCJpYXQiOjE1ODI4ODE2MjgsImV4cCI6MTg5ODI0MTYyOH0.P4DAjXyODV8cFPgObaULjAMPg-7xSbUsVJ8Ohp7xTQI'
