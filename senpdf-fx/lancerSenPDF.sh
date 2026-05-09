#!/bin/bash
echo "Compilation de SenPDF..."
/usr/lib/jvm/java-11-openjdk-amd64/bin/javac \
    --module-path /usr/share/java/javafx-controls-11.jar:/usr/share/java/javafx-base-11.jar:/usr/share/java/javafx-graphics-11.jar \
    --add-modules javafx.controls,javafx.base,javafx.graphics \
    -d . \
    src/SenPDF.java

if [ $? -eq 0 ]; then
    echo "Lancement de SenPDF..."
    /usr/lib/jvm/java-11-openjdk-amd64/bin/java \
        --module-path /usr/share/java/javafx-controls-11.jar:/usr/share/java/javafx-base-11.jar:/usr/share/java/javafx-graphics-11.jar \
        --add-modules javafx.controls,javafx.base,javafx.graphics \
        SenPDF
else
    echo "Erreur de compilation !"
fi
