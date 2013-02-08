from fabric.api import run, sudo, cd, settings, task, put, prompt
from fabric.colors import yellow, green
from fabric.contrib.files import exists
from StringIO import StringIO
from configs import nginx_server_config, nginx_client_config, nginx_ssl_config, openseedbox_server_config, openseedbox_client_config

packages = "unzip git openjdk-6-jre-headless python-software-properties build-essential libssl-dev libpcre3-dev nginx"
server_only_packages = "transmission-daemon cryptsetup"
play_location = "http://download.playframework.org/releases/play-1.2.5.zip"
nginx_location = "http://nginx.org/download/nginx-1.2.6.tar.gz"
repositories = [
	[ "openseedbox-common", "http://unsignedint@bitbucket.org/unsignedint/openseedbox-common.git" ],
	[ "openseedbox-server", "http://unsignedint@bitbucket.org/unsignedint/openseedbox-server.git" ],
	[ "openseedbox", "http://unsignedint@bitbucket.org/unsignedint/openseedbox.git" ]
] #Using array of arrays instead of map because order needs to be preserved

openseedbox_backend_port=9001
openseedbox_client_port=9000
openseedbox_backend_path="/media/openseedbox"
openseedbox_ssl_enabled=True
openseedbox_client_db_url="jdbc:mysql://root:S*a!m()$@localhost/openseedbox"

@task
def bootstrap(type="server", server_name="localhost", server_api_key=""):
	print(green("Bootstrapping %s node" % type))
	step = 1
	text("%s. Creating /src directory" % step)
	sudo("mkdir -p /src")
	sudo("chmod -R 775 /src")
	user = run("whoami")
	sudo("chown -R %s /src" % user)	
	step += 1
	
	text("%s. Updating apt package repo and installing required packages" % step)
	sudo("apt-get update -qq")
	all_packages = "%s %s" % (packages, server_only_packages) if (type=="server") else packages
	sudo("apt-get install -qqy %s" % all_packages)
	if (type == "server"):
		#We cant install updated transmission from ppa until python-software-properties was installed in the previous step
		sudo("apt-add-repository -y ppa:transmissionbt/ppa")
		sudo("apt-get update -qq")
		sudo("apt-get install -qqy transmission-daemon")
	with settings(warn_only=True):
		sudo("apt-mark hold nginx") #incase there is an nginx update in the repos, we still want to use the version we compile below
	step += 1
		
	with cd("/src"):
		text("%s. Installing Play 1.2.5" % step)
		run("wget -q %s" % play_location)
		run("unzip -qqo *.zip")
		run("rm *.zip")
		sudo("ln -fs /src/play*/play /usr/bin/play")
		step += 1
		
		text("%s. Writing out ivysettings.xml" % step)
		run("mkdir -p ~/.ivy2")
		#the following ivy settings fixes a bug where `play deps` hangs on downloading
		put(StringIO("<ivysettings><settings httpRequestMethod='GET' /></ivysettings>"), "~/.ivy2/ivysettings.xml");
		step += 1
		
		text("%s. Checking out git repositories" % step)
		pull_or_clone()
		step += 1
	
	if (type == "server"):	
		if not exists("~/data.img"):
			text("%s. Creating encrypted partion" % step)
			size = prompt("How many GB do you want the partition to be?")
			run("fallocate -l %sG ~/data.img" % size)
			sudo("cryptsetup --verify-passphrase --hash=sha256 luksFormat ~/data.img")
			sudo("cryptsetup luksOpen ~/data.img openseedbox")
			sudo("mkfs.ext4 /dev/mapper/openseedbox")
			sudo("mkdir /media/openseedbox")
			sudo("mount /dev/mapper/openseedbox /media/openseedbox")
			step += 1
	
		text("%s. Mounting encrypted partition and setting permissions" % step)	
		mount_encrypted_partition()		
		sudo("chmod -R 775 /media/openseedbox")
		sudo("chown -R %s /media/openseedbox" % user)
		step += 1
	
		text("%s. Removing transmission-daemon from startup" % step)
		sudo("update-rc.d -f transmission-daemon remove")
		step += 1
		
	text("%s. Downloading and compiling custom nginx" % step) #note: we installed nginx via apt-get before because we wanted to get the init.d scripts
	sudo("service nginx stop", pty=False)
	with cd("/src"):
		run("wget -q %s" % nginx_location)
		run("tar -xvf nginx*.tar.gz")
		run("rm nginx*.tar.gz")
		if not exists("headers-more-nginx-module"):
			run("git clone -q https://github.com/agentzh/headers-more-nginx-module.git") #get headers-more nginx module
		else:
			run("cd headers-more-nginx-module && git pull")
		if not exists("mod_zip"):
			run("git clone -q https://github.com/evanmiller/mod_zip.git") #get mod_zip ngnx module
		else:
			run("cd mod_zip && git pull")			
	with cd("/src/nginx*"):
		run("./configure --with-http_ssl_module --add-module=/src/mod_zip/ --prefix=/etc/nginx --conf-path=/etc/nginx/nginx.conf --error-log-path=/var/log/nginx/error.log --pid-path=/var/run/nginx.pid --http-log-path=/var/log/nginx/access.log --lock-path=/var/lock/nginx.lock --sbin-path=/usr/sbin/nginx --add-module=/src/headers-more-nginx-module")
		run("make")
		sudo("make install")
	sudo("service nginx start", pty=False)
	step += 1
	
	folder = get_folder(type)
	text("%s. Creating nginx config for %s" % (step, folder))
	create_nginx_config(type, server_name)
	step += 1
	
	text("%s. Creating play config for %s" % (step, folder))
	create_play_config(type, server_api_key)
	step += 1	
	
	text("%s. Starting play server for %s" % (step, folder))
	start_play(type)

@task
def update_code():
	print(green("Updating code"))
	with cd("/src"):
		pull_or_clone()
		
@task
def start_play(type="server"):
	folder = get_folder(type)
	stop_play(type)
			
	with cd("/src/%s" % folder):
		run("play start --%prod", pty=False)
		
	text("%s started!" % folder)
	
@task
def update_servers():
	text("Updating apt package cache and packages")
	sudo("apt-get update -qq")
	sudo("apt-get upgrade -qqy")
	
	text("Updating source repositories")
	with cd("/src"):
		pull_or_clone()		
		
	text("Restarting nginx and play services")
	
@task
def update_configs(type="server", server_name="localhost", server_api_key=""):
	text("Stopping play servers and nginx")
	stop_play(type)
	sudo("service nginx stop", pty=False)
	text("Updating play and nginx configs")
	create_nginx_config(type, server_name)
	create_play_config(type, server_api_key)
	text("Starting play servers and nginx")
	start_play(type)
	sudo("service nginx start", pty=False)
	
def stop_play(type="server"):
	folder = get_folder(type)
	pid = ""
	with cd("/src/%s" % folder):
		if (exists("server.pid")):
			pid = run("cat server.pid")
			pid = pid.strip()			
		if pid:
			with settings(warn_only=True):			
				run("kill -9 %s" % pid)	
				run("rm server.pid")	
	
def pull_or_clone():
	#note: assumes caller is using 'with cd("/src"):'
	for r in repositories:
		name = r[0]
		repo = r[1]
		if not exists(name):
			run("git clone -q %s /src/%s" % (repo, name))
		else:
			with cd("/src/%s" % name):
				run("git pull")
		with cd("/src/%s" % name):
			run("play deps --sync")
				
def mount_encrypted_partition():
	if not exists("/dev/mapper/openseedbox"):
		sudo("cryptsetup luksOpen ~/data.img openseedbox")
		sudo("mount /dev/mapper/openseedbox /media/openseedbox")

def create_nginx_config(type="server", servername=""):
	folder = get_folder(type)
	sudo("rm -fr /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default")
	if not servername:
		servername = prompt("What is this servers servername?")
	replace = get_config_params(servername)
	config = nginx_server_config if (type == "server")	else nginx_client_config
	put(StringIO(config % replace), "/etc/nginx/sites-available/%s" % folder, use_sudo=True)
	sudo("ln -fs /etc/nginx/sites-available/%s /etc/nginx/sites-enabled/%s" % (folder, folder))
	sudo("service nginx restart", pty=False)

def create_play_config(type="server", server_api_key=""):
	folder = get_folder(type)
	if (type == "server") and not server_api_key:
		server_api_key = prompt("What is this servers api_key?")
	replace = get_config_params()
	replace["api_key"] = server_api_key
	config = openseedbox_server_config if (type == "server")	else openseedbox_client_config
	put(StringIO(config % replace), "/src/%s/conf/application.conf" % folder)

def get_folder(type):
	return "openseedbox-server" if (type == "server") else "openseedbox"
	
def get_config_params(server_name=""):
	ssl = nginx_ssl_config if openseedbox_ssl_enabled else ""
	nginx_port = "443" if openseedbox_ssl_enabled else "80"
	return { "ssl" : ssl, "server_name" : server_name, "backend_port" : openseedbox_backend_port,
				"backend_path" : openseedbox_backend_path, "nginx_port" : nginx_port,
				"client_port" : openseedbox_client_port, "client_db_url" : openseedbox_client_db_url } 
	
def text(t):
	print(yellow(t))
