#!/bin/sh
rm -r gen-java/com/infogen/thrift/*
rm -r src/main/java/com/infogen/thrift/*
thrift -r --gen java thrift/Message.thrift
cp -r gen-java/com/infogen/thrift/* src/main/java/com/infogen/thrift