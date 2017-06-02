#!/bin/bash
set -e
set -o pipefail

archs='aarch64 amd64 armv7hf'
for arch in $archs; do
	baseImage='openseedbox/client'
	if [ "$arch" == 'amd64' ]; then
		suite='latest';
	else
		suite=$arch;
	fi;

	dockerfile=$arch.Dockerfile
	sed -e s~#{FROM}~$baseImage:$suite~g \
		Dockerfile.tpl > $dockerfile
done
