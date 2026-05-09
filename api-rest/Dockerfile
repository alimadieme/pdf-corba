FROM eclipse-temurin:8-jdk
WORKDIR /app
COPY . /app
RUN idlj -fall PdfService.idl && javac -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar:mongodb-driver-sync.jar:mongodb-driver-core.jar:bson.jar:slf4j-api.jar PdfServiceApp/*.java PdfServiceImpl.java StartServer.java PdfController.java
EXPOSE 8080
CMD bash -c "orbd -ORBInitialPort 1050 & sleep 3 && java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar:mongodb-driver-sync.jar:mongodb-driver-core.jar:bson.jar:slf4j-api.jar StartServer -ORBInitialPort 1050 -ORBInitialHost localhost & sleep 5 && java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar:mongodb-driver-sync.jar:mongodb-driver-core.jar:bson.jar:slf4j-api.jar PdfController"
