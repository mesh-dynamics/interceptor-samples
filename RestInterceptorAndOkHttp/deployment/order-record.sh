#kubectl delete -f /Users/muthumani/cube/Development/git/interceptor-samples/RestInterceptorAndOkHttp/deployment/order-receiver-dep-replay.yaml 

#kubectl apply -f /Users/muthumani/cube/Development/git/interceptor-samples/RestInterceptorAndOkHttp/deployment/order-receiver-dep.yaml 

echo "Enter collection name"
read COLLECTION_NAME

RESPONSE="$(curl -X POST \
  http://demo.dev.cubecorp.io/cs/start/OrderCN/OrderApp/test-order/$COLLECTION_NAME/DEFAULT \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H 'cache-control: no-cache' \
  -d "name=$COLLECTION_NAME&userId=OrderUser" )"

echo $RESPONSE

RECORDING_ID=$(echo $RESPONSE | sed 's/^.*"id":"\([^"]*\)".*/\1/')

echo "RECORDING_ID:" $RECORDING_ID

#sleep 90

#Change the AWS domain name as appropriate
curl -X GET  'http://a8d9f3d8c1a9311ea9b500234fefd3a9-1911613861.us-east-2.elb.amazonaws.com:8080/orders/' -H 'x-b3-traceid: 14766e909f539fd1e9ebba339efe313a' -H 'x-b3-spanid: 14766e909f539fd1e9ebba339efe313a'
sleep 2

curl -X POST \
  http://a8d9f3d8c1a9311ea9b500234fefd3a9-1911613861.us-east-2.elb.amazonaws.com:8080/orders/ \
  -H 'Accept: application/json' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 204' \
  -H 'Content-Type: application/json' \
  -H 'Host: a8d9f3d8c1a9311ea9b500234fefd3a9-1911613861.us-east-2.elb.amazonaws.com:8080' \
  -H 'x-b3-traceid: 14766e909f539fd1e9ebba339efe253a' \
  -H 'x-b3-spanid: 14766e909f539fd1e9ebba339efe253a' \
  -H 'cache-control: no-cache' \
  -d '        {
            "productId": 1,
            "customer": {
                "firstName": "Rajesh",
                "lastName": "Kumar",
                "email": "234@gmail.com"
            }
        }'

sleep 2

curl -X GET  'http://a8d9f3d8c1a9311ea9b500234fefd3a9-1911613861.us-east-2.elb.amazonaws.com:8080/orders/'  -H 'x-b3-traceid: 14766e909f539fd1e9ebba339efe443a' -H 'x-b3-spanid: 14766e909f539fd1e9ebba339efe443a'

sleep 20

curl -X POST http://demo.dev.cubecorp.io/cs/stop/$RECORDING_ID

sleep 10
