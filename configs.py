#Nginx config for openseedbox-server
nginx_server_config = """
server {
	listen %(nginx_port)s;
	server_name %(server_name)s;

	%(ssl)s

	merge_slashes off;
	proxy_method GET;

	location / {		
		proxy_set_header Host $http_host;
		proxy_pass http://localhost:%(backend_port)s;
		more_clear_headers "Set-Cookie" "X-Archive-Files";
	}

	location /protected {
		internal;
		alias %(backend_path)s/complete/;
	}
}
"""

#Nginx config for openseedbox
nginx_client_config = """
server {
	listen 80;
	server_name %(server_name)s;
	rewrite ^ https://$server_name$request_uri? permanent;
}

server {
	listen 443;
	server_name %(server_name)s;
	
	%(ssl)s

	location / {
		proxy_set_header Host $http_host:$http_port;
		proxy_pass http://localhost:%(client_port)s;
	}

	location ~* ^/rdr/(.*?)/(.*) {
		internal;
		resolver 8.8.8.8;
		proxy_set_header Host $1;
		proxy_max_temp_file_size 0;
		proxy_pass $arg_scheme://$1/$2?$args;
		more_clear_headers "Set-Cookie" "X-Archive-Files";
	}
}
"""

#Nginx config for generic ssl section
nginx_ssl_config = """
	ssl on;
	ssl_certificate /src/openseedbox/conf/host.cert;
	ssl_certificate_key /src/openseedbox/conf/host.key;
"""

#Play! config for openseedbox-server
openseedbox_server_config = """
#Application
application.name=openseedbox-server
application.mode=prod
application.secret=olHnjc5fXP8EIUAiEdi5EyrnjFbf9mZxxKBqnNdcpJhfGFHP514T4P44gYrBgwFV
date.format=yyyy-MM-dd

#Http settings
http.port=%(backend_port)s
http.address=127.0.0.1

#Openseedbox Backend Settings
backend.base.api_key=%(api_key)s
backend.base.path=%(backend_path)s
backend.base.device=%(backend_path)s
backend.class=com.openseedbox.backend.transmission.TransmissionBackend
backend.download.scheme=https
backend.download.xsendfile=true
backend.download.xsendfile.path=/protected
backend.download.xsendfile.header=X-Accel-Redirect
backend.download.ngxzip=true
backend.download.ngxzip.path=/protected
backend.download.ngxzip.manifestonly=false
backend.blocklist.url=http://list.iblocklist.com/?list=bt_level1&fileformat=p2p&archiveformat=gz
openseedbox.assets.prefix=https://s3.amazonaws.com/cdn.openseedbox.com

#Transmission backend settings
backend.transmission.port=1234
"""

#Play! config for openseedbox
openseedbox_client_config = """
application.name=openseedbox
application.mode=prod
application.secret=isnONYuAmgrglqEEiCujUnm0xtGHYkTspt8FzHQ6h4GDVoZ7KyxhPwH50itwFVvd
date.format=yyyy-MM-dd

application.log=DEBUG

db.url=%(client_db_url)s
db.driver=com.mysql.jdbc.Driver
siena.ddl=update

http.port=9000
http.address=127.0.0.1

#%%prod.memcached=enabled
#%%prod.memcached.host=127.0.0.1:11211

mail.smtp.host=smtp.openseedbox.com
mail.smtp.user=admin@openseedbox.com
mail.smtp.pass=S*a!m()$
mail.smtp.channel=ssl

#OpenSeedbox-specific config
openseedbox.node.access=https
openseedbox.errors.mailto=erin.dru@gmail.com
openseedbox.errors.mailfrom=Openseedbox Errors <errors@openseedbox.com>
openseedbox.zip=true
openseedbox.zip.path=/rdr
openseedbox.zip.manifestonly=false
openseedbox.assets.prefix=https://s3.amazonaws.com/cdn.openseedbox.com/public/
"""
