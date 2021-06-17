#! /usr/bin/env bash
# SPDX-License-Identifier: zlib-acknowledgement

# -d bin
# src/*.java
javac -Xlint --module-path /snap/openjfx/current/sdk/lib \
        --add-modules javafx.controls,javafx.fxml \
        Breakpoint.java BinaryFileReader.java
