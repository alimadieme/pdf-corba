FROM eclipse-temurin:8-jdk
WORKDIR /app

COPY . /app

# Cache buster
RUN echo "build-v3"

RUN apt-get update && apt-get install -y curl && \
    curl -L -o commons-logging.jar \
    https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar

RUN idlj -fall PdfService.idl

RUN javac -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    PdfServiceApp/*.java PdfServiceImpl.java StartServer.java PdfController.java

RUN java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    CreateTestPDF 2>/dev/null || true

EXPOSE 8080

CMD bash -c "orbd -ORBInitialPort 1050 & \
    sleep 3 && \
    java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    StartServer -ORBInitialPort 1050 -ORBInitialHost localhost & \
    sleep 5 && \
    java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    PdfController"
EOF
