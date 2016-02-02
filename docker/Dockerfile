FROM openseedbox

RUN apt-get install -qq -y transmission-daemon \
	&& apt-get -y clean

WORKDIR /src/openseedbox-common
RUN /play/play deps

WORKDIR /src/openseedbox-server
RUN /play/play deps

VOLUME /media/openseedbox

COPY application.conf conf/application.conf
COPY run.sh /run.sh
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 443
