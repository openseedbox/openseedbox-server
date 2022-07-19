# syntax=docker/dockerfile:1

FROM openseedbox/base as builder

RUN apt update -qq && apt install -y --no-install-recommends \
	git ca-certificates python;

ARG CACHEBUSTER
RUN bash -c "for repo in openseedbox-{common,server}; do echo updating git for \$repo; cd /src/\$repo; git pull --ff-only origin master; done"
RUN bash -c "for repo in openseedbox-{common,server}; do echo updating play deps for \$repo; cd /src/\$repo; /play/play deps --sync --forProd; done"

COPY application.conf /src/openseedbox-server/conf/application.conf
RUN touch /src/openseedbox-server/conf/application.runtime.conf
RUN > /src/openseedbox-server/.dependencies; for package in \
	procps transmission-daemon curl; do \
		echo $package >> /src/openseedbox-server/.dependencies.txt; \
	done

WORKDIR /src
RUN /play/play war openseedbox-server -o war
# Remove old Play! bundled jars
RUN bash -c "rm -fv war/WEB-INF/lib/{postgresql-9,log4j-1,slf4j-log4j12-1}.*.jar;"

# Remove irrelevant application directories
RUN bash -c "rm -rfv war/WEB-INF/application/{docker,test,javadoc,lib}"

# (Optional) Suppress log messages "At least one JAR was scanned for TLDs yet contained no TLDs." during Tomcat startup
RUN mkdir war/META-INF; \
	echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<Context>\n\
    <JarScanner>\n\
        <JarScanFilter tldSkip="*.*"/>\n\
    </JarScanner>\n\
</Context>' > war/META-INF/context.xml;
# Only necessary if using Tomcat >= 10.0 due to package name change from javax.* to jakarta.*
RUN bash -c "mv -v war/WEB-INF/application/precompiled/java/play war/WEB-INF/classes"


FROM builder as tester
# Autotest OpenSeedbox
WORKDIR /src/openseedbox-server
# Extra packages for OpenSeedbox
RUN set -eux; \
	apt-get update; \
	xargs -rt apt-get install -y --no-install-recommends < .dependencies.txt;
# sync again but without --forProd to include openseedbox-common tests
RUN /play/play deps --sync
RUN RANDOM_HTTP_PORT=$(shuf -i 10000-59152 -n 1); sed -i -e 's/${RANDOM_HTTP_PORT}'"/$RANDOM_HTTP_PORT/" conf/application.conf; echo random port is $RANDOM_HTTP_PORT
RUN /play/play autotest || true


FROM openseedbox/tomcat
# Extra packages for OpenSeedbox
COPY --from=builder /src/openseedbox-server/.dependencies.txt /tmp
RUN set -eux; \
	apt-get update; \
	xargs -rt apt-get install -y --no-install-recommends < /tmp/.dependencies.txt; \
	rm -rf /var/lib/apt/lists/*

# Default values for config environment variables
ENV OPENSEEDBOX_SESSION_COOKIE_NAME=NOT_GENERATED
ENV APPLICATION_SECRET=NOT_GENERATED
ENV OPENSEEDBOX_API_KEY=

# Copy WAR file from builder container to the final image
COPY --from=builder /src/war $CATALINA_HOME/webapps/ROOT
