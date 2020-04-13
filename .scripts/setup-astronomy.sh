# https://github.com/Azure-Samples/java-on-aks
# https://docs.microsoft.com/en-us/azure/aks/kubernetes-walkthrough

az login

az group create --name ${RESOURCE_GROUP} --location ${REGION}

az acr create --name ${CONTAINER_REGISTRY} --resource-group ${RESOURCE_GROUP} --sku standard --location ${REGION}

# https://www.digitalocean.com/community/questions/how-to-fix-docker-got-permission-denied-while-trying-to-connect-to-the-docker-daemon-sockethttps://www.digitalocean.com/community/questions/how-to-fix-docker-got-permission-denied-while-trying-to-connect-to-the-docker-daemon-socket
# added to sudoers
# alias docker="sudo /usr/bin/docker"
# sudo usermod -a -G docker $USER
# sudo chown "$USER":"$USER" /home/"$USER"/.docker -R
# sudo chmod g+rwx "$HOME/.docker" -R
# sudo chmod 666 /var/run/docker.sock
# newgrp docker
az acr login -n ${CONTAINER_REGISTRY}

az aks create --name ${AKS_CLUSTER} --resource-group ${RESOURCE_GROUP} --location ${REGION} --attach-acr ${CONTAINER_REGISTRY} --node-vm-size Standard_D2_v3 --node-count 2

# updates ~/.kube/config
az aks get-credentials --name ${AKS_CLUSTER} --resource-group ${RESOURCE_GROUP}

# build and deploy to azure container registry
mvn jib:build # astronomy
mvn jib:build # cd demo-server

az acr repository list --name astronomycontainerregistry

CLIENT_ID=$(az aks show -g ${RESOURCE_GROUP} -n ${AKS_CLUSTER} --query servicePrincipalProfile.clientId -o tsv)
az role assignment create --assignee $CLIENT_ID --role "Network Contributor" --scope /subscriptions/8d289e83-ca59-4410-aeaa-2605014b27c8/resourceGroups/astronomy-resource-group

# https://docs.microsoft.com/en-us/azure/aks/update-credentials
SP_ID=$(az aks show --resource-group ${RESOURCE_GROUP} --name ${AKS_CLUSTER} --query servicePrincipalProfile.clientId -o tsv)
SP_SECRET=$(az ad sp credential reset --name $SP_ID --query password -o tsv)
az aks update-credentials --resource-group ${RESOURCE_GROUP} --name ${AKS_CLUSTER} --reset-service-principal --service-principal $SP_ID --client-secret $SP_SECRET

source ../.scripts/prepare-kubernetes-manifest-files.sh

kubectl apply -f deploy/1-demo-service.yaml
kubectl apply -f deploy/2-astronomy-service.yaml

kubectl get services

kubectl describe svc demo-service
--> "...  If access was recently granted, please refresh your credentials." ?

#
http://comgithubclojjaksastronomy.westus2.cloudapp.azure.com:8080/demo/hello

kubectl get events --all-namespaces

# update deployments
export DEMO_SERVER_VERSION=v2
kubectl set image deployments/demo-service demo-service=astronomycontainerregistry.azurecr.io/demo-server:dev-$DEMO_SERVER_VERSION