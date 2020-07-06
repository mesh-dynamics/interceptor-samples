sudo docker stop springandrest_receiver_1
sudo docker stop transformer
sudo docker image rm -f cubeiocorp/sample-order-transformer
sudo docker image rm -f cubeiocorp/sample-order-receiver
sudo docker-compose rm
