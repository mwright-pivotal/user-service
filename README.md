# user-service
This service is a spring boot application responsible for managing users of Tanzu Bank

User Service depends on an OAuth2 provider.  In this case we are using UAA.  Instructions for deploying a custom UAA to Kubernetes can be found here:  https://github.com/cloudfoundry/uaa

Steps to deploy.

1. Deploy UAA to the default namespace where you have deployed cf4k8s.  Be sure to set values for your admin user and desired database.
2. To access the UAA cli, run kubectl run --generator=run-pod/v1 tmp-shell --rm -i --tty --image governmentpaas/cf-uaac -- /bin/sh
3. copy/paste the commands from scripts/uaa-setup found in this repo into the terminal window to configure UAA
4. As of this repo creation, all CF workloads get deployed to cf-workloads.  Edit configmap k8s/user-service-configmap.yaml to match your values for UAA.  Create ConfigMap in namespace cf-workloads
5. User Service uses Spring Cloud Kubernetes Config to process config maps.  In order to do so, Spring needs a service account and your k8s cert info so it can read configmaps in the cf-workloads namespace.   Update manifest-noservice.yml to reflect your creds created for the application.  For demo purposes you can pull these values from your KUBECONFIG.
 
