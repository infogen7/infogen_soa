#!/bin/sh
rm -r gen-java/com/infogen/grpc/*
rm -r src/main/java/com/infogen/grpc/*
protoc --java_out=gen-java protobuf/Message.proto
cp -r gen-java/com/infogen/grpc/* src/main/java/com/infogen/grpc/