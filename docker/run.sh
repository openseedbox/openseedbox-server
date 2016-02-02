#!/bin/bash
cd /src/openseedbox-server

if [ "`grep "application.secret=NOT_GENERATED" conf/application.conf`" != "" ]; then
	echo "Play secret has not been generated; generating"
	/play/play secret
fi

echo "Starting play"
exec /play/play run
