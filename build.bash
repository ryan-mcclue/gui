#! /usr/bin/env bash
# SPDX-License-Identifier: zlib-acknowledgement

[[ ! -d bin ]] && mkdir bin

JAVAFX_LIB_PATH="/snap/openjfx/current/sdk/lib"

javac -Xlint src/BinaryFileReader.java src/Breakpoint.java src/MOFile.java -d bin \
        --module-path ${JAVAFX_LIB_PATH} \
        --add-modules javafx.controls,javafx.fxml 
