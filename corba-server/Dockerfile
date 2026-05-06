FROM eclipse-temurin:8-jdk
WORKDIR /app

COPY . /app

RUN idlj -fall PdfService.idl

RUN javac -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    PdfServiceApp/*.java \
    PdfServiceImpl.java \
    StartServer.java

EXPOSE 1050
COPY index.html /app/index.html
CMD bash -c "orbd -ORBInitialPort 1050 & sleep 3 && \
    java -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    StartServer -ORBInitialPort 1050 -ORBInitialHost localhost"
