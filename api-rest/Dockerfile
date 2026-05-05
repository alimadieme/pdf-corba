FROM eclipse-temurin:8-jdk
WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y curl

RUN curl -L -o commons-logging.jar \
    https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar

RUN idlj -fall PdfService.idl

RUN javac -cp .:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar \
    PdfServiceApp/*.java \
    PdfController.java

EXPOSE 8080

CMD ["java", "-cp", \
     ".:pdfbox-1.8.16.jar:fontbox-1.8.16.jar:jempbox-1.8.16.jar:commons-logging.jar", \
     "PdfController"]
