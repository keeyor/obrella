mvn clean package && mkdir .\target\extracted && java -Djarmode=layertools -jar .\target\opendelos-live-app.jar extract --destination .\target\extracted && docker build -t opendelos-live-app . && docker tag opendelos-live-app  michaelgatzonis/opendelos-live-app:0.1 && docker push michaelgatzonis/opendelos-live-app:0.1