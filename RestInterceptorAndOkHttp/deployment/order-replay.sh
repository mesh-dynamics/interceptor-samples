#kubectl delete -f /Users/muthumani/cube/Development/git/interceptor-samples/RestInterceptorAndOkHttp/deployment/order-receiver-dep.yaml
#kubectl apply -f /Users/muthumani/cube/Development/git/interceptor-samples/RestInterceptorAndOkHttp/deployment/order-receiver-dep-replay.yaml

echo "Enter the Recording Id"
read RECORDING_ID

#sleep 90

#change the replay endpoint appropriately
REPLAY_RESPONSE="$(curl -X POST http://demo.dev.cubecorp.io/rs/start/$RECORDING_ID \
  -d 'instanceId=test-order&templateSetVer=DEFAULT&userId=OrderUser&endPoint=http://a8d9f3d8c1a9311ea9b500234fefd3a9-1911613861.us-east-2.elb.amazonaws.com:8080&paths=orders/getOrders/&paths=orders/postOrder/' \
  -H 'content-type: application/x-www-form-urlencoded' )"

sleep 60

REPLAY_ID=$(echo $REPLAY_RESPONSE |  sed 's/^.*"replayId":"\([^"]*\)".*/\1/')

curl -X POST http://demo.dev.cubecorp.io/as/analyze/$REPLAY_ID -H 'content-type: application/x-www-form-urlencoded'
