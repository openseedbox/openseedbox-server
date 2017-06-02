FROM openseedbox/client:latest

RUN apt-get -qq update \
	&& apt-get install -qq -y transmission-daemon \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Update openseedbox-common (from openseedbox/client) and clone openseedbox-server
RUN git --work-tree=/src/openseedbox-common --git-dir=/src/openseedbox-common/.git pull \
	&& /play/play deps /src/openseedbox-common --sync \
	&& git clone --depth=1 -q https://github.com/openseedbox/openseedbox-server /src/openseedbox-server \
	&& /play/play deps /src/openseedbox-server --sync

VOLUME /media/openseedbox

COPY application.conf /src/openseedbox-server/conf/application.conf
COPY run.sh /run.sh
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 443
