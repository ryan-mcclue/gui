#! /usr/bin/env bash
# SPDX-License-Identifier: zlib-acknowledgement

JAVAFX_LIB_PATH="/snap/openjfx/current/sdk/lib"
java -cp bin BinaryFileReader \
     --module-path ${JAVAFX_LIB_PATH} \ 
     --add-modules javafx.controls,javafx.fxml
      
