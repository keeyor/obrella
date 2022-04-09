mvn clean package && mkdir .\target\extracted && java -Djarmode=layertools -jar .\target\opendelos-events-app.jar extract --destination .\target\extracted && docker build -t opendelos-events-app . && docker tag opendelos-events-app  michaelgatzonis/opendelos-events-app:0.1 && docker push michaelgatzonis/opendelos-events-app:0.1