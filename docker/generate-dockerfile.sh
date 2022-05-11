#!/bin/bash
set -e
set -o pipefail

archs='aarch64 amd64 armv7hf'
for arch in $archs; do
	suite="$arch"
	case "$arch" in
		aarch64)
			# Not needed on Travis CI (but https://github.com/openseedbox/openseedbox/issues/70 exists)
			#balenaCrossBuildBegin='RUN [ "cross-build-start" ]'
			#balenaCrossBuildEnd='RUN [ "cross-build-end" ]'
			;;
		armv7hf)
			# It's still needed, as armv7hf is still bogous on Travis CI
			balenaCrossBuildBegin='RUN [ "cross-build-start" ]'
			balenaCrossBuildEnd='RUN [ "cross-build-end" ]'
			;;
		amd64)
			balenaCrossBuildBegin=''
			balenaCrossBuildEnd=''
			suite='latest'
			;;
	esac;

	dockerfile=$arch.Dockerfile
	sed -e s~#{ARCH}~$suite~g \
		-e s~#{BALENA_CROSSBUILD_BEGIN}~"$balenaCrossBuildBegin"~g \
		-e s~#{BALENA_CROSSBUILD_END}~"$balenaCrossBuildEnd"~g \
		Dockerfile.tpl > $dockerfile
done
