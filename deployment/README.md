#Apply fluentd Config

1. Apply configMap
```
kubectl apply -f fluentd_configMap.yaml
```

2. Apply path to add volume mount
```
kubectl patch ds fluentd --type=json --patch "$(cat fluentd_patch.json)" -n logging --record
```