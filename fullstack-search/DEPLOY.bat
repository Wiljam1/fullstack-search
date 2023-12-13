@echo off

set SERVICE_NAME=fullstack-search
set IMAGE_NAME=wiljam/%SERVICE_NAME%
set PORT_NUMBER=8080

docker build -f src/main/docker/Dockerfile.jvm -t %IMAGE_NAME%:latest .
docker push %IMAGE_NAME%:latest

kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl rollout restart deployment %SERVICE_NAME%

echo Deployment completed, starting port-forward...

:portforward
echo Starting port-forward...
kubectl port-forward service/%SERVICE_NAME% %PORT_NUMBER%:%PORT_NUMBER%

if %errorlevel% neq 0 (
    echo Port-forwarding failed. Retrying in 3 seconds...
    timeout /nobreak /t 3 > nul
    goto portforward
)

echo Port-forwarding successful.
