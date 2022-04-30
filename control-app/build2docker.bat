mvn clean package && mkdir .\target\extracted && java -Djarmode=layertools -jar .\target\opendelos-control-app.jar extract --destination .\target\extracted && docker build -t opendelos-control-app . && docker tag opendelos-control-app  michaelgatzonis/opendelos-control-app:0.1 && docker push michaelgatzonis/opendelos-control-app:0.1