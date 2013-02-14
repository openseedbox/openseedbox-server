from fabric.api import run, sudo, cd, settings, task, put, prompt, abort
from fabric.colors import yellow, green
from fabric.contrib.files import exists
from fabric.contrib.console import confirm
from StringIO import StringIO
from configs import nginx_server_config, nginx_client_config, openseedbox_server_config, openseedbox_client_config

packages = "unzip git openjdk-6-jre-headless python-software-properties build-essential libssl-dev libpcre3-dev"
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
openseedbox_client_db_url="jdbc:mysql://localhost/openseedbox?user=sites&password=sitespassword"

@task
def bootstrap(type="server", server_name="localhost", server_api_key="", configure_nginx=True, encrypt=True):
	"""
	Bootstraps a new OpenSeedbox node
	
	Args:
		type:
		either "client" or "server". "client" is for the frontend
		website node, "server" is for the backend torrenting nodes.
		
		server_name:
		defaults to "localhost". This is what is put in the webserver
		config file.
		
		server_api_key:
		Only applies when type is set to "server". This is written to the Play!
		config file and is used by the client to communicate with this server.
		
		configure_nginx: 
		defaults to True. When set, a custom nginx will be downloaded and
		compiled, and nginx config files will be written out.
		
		encrypt:
		Only applies when type is set to "server". Defaults to True. When True,
		a dm-crypt encrypted mountpoint will be created to store torrent data
		on.
	"""
	#Fabric passes everything as strings, so make sure we can use the following properly in IF statements
	configure_nginx = str2bool(configure_nginx)
	encrypt = str2bool(encrypt)
	folder = get_folder(type)
	
	print(green("Bootstrapping %s node" % type))
	text("""
Node Type: %s
Server Name: %s
Server API Key: %s
Configure NGINX: %s
Configure Encrypted Folder: %s""" % (type, server_name, server_api_key, configure_nginx, encrypt))
			 
	if not confirm("Are these settings all good?"):
		abort("Aborting to prevent injury.")
	
	step = 1	
	text("%s. Removing /src for fresh install" % step)
	sudo("rm -fr /src")
	step += 1
		
	text("%s. Creating /src directory" % step)
	sudo("mkdir -p /src")
	sudo("chmod -R 775 /src")
	user = run("whoami")
	sudo("chown -R %s /src" % user)	
	step += 1
	
	text("%s. Updating apt package repo and installing required packages" % step)
	sudo("apt-get update -qq")
	all_packages = "%s %s" % (packages, server_only_packages) if type == "server" else packages
	if configure_nginx:
		all_packages += " nginx"
	sudo("apt-get install -qqy %s" % all_packages)
	if type == "server":
		#We cant install updated transmission from ppa until python-software-properties was installed in the previous step
		sudo("apt-add-repository -y ppa:transmissionbt/ppa")
		sudo("apt-get update -qq")
		sudo("apt-get install -qqy transmission-daemon")
	if configure_nginx:
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
		pull_or_clone(type)
		step += 1
	
	if type == "server":
		text("%s. Creating mountpoint %s" % (step, openseedbox_backend_path));		
		sudo("mkdir -p %s" % openseedbox_backend_path)
		step += 1
	
		if encrypt:
			if not exists("~/data.img"):
				text("%s. Creating encrypted partion" % step)
				size = prompt("How many GB do you want the partition to be?")
				run("fallocate -l %sG ~/data.img" % size)
				sudo("cryptsetup --verify-passphrase --hash=sha256 luksFormat ~/data.img")
				sudo("cryptsetup luksOpen ~/data.img openseedbox")
				sudo("mkfs.ext4 /dev/mapper/openseedbox")
				sudo("mount /dev/mapper/openseedbox %s" % openseedbox_backend_path)
				step += 1
	
			text("%s. Mounting encrypted partition" % step)	
			mount_encrypted_partition()
			step += 1
		
		text("%s. Setting permissions on %s" % (step, openseedbox_backend_path))
		sudo("chmod -R 775 %s" % openseedbox_backend_path)
		sudo("chown -R %s %s" % (user, openseedbox_backend_path))
		step += 1
			
		text("%s. Removing transmission-daemon from startup" % step)
		sudo("update-rc.d -f transmission-daemon remove")
		step += 1	
	
	if configure_nginx:
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
			
		text("%s. Creating nginx config for %s" % (step, folder))
		create_nginx_config(type, server_name) #note: this restarts nginx
		step += 1
	
	text("%s. Creating play config for %s" % (step, folder))
	create_play_config(type, server_api_key)
	step += 1	
	
	text("%s. Starting play server for %s" % (step, folder))
	start_play(type, encrypt)

@task
def update_code(type="server", encrypt=True):
	"""
	Updates the OpenSeedbox code and restarts the Play! services
	
	Args:
		type:
		Either "client" or "server" (defaults to "server"). This sets what
		git repositories to update.
		
		encrypt:
		Whether or not to make sure encrypted partition is mounted. Defaults to True
	"""
	encrypt = str2bool(encrypt)
	print(green("Updating code for type: %s" % type))
	with cd("/src"):
		pull_or_clone(type)
	print(green("Restarting play for type: %s" % type))
	start_play(type, encrypt) #start_play calls stop_play first
		
@task
def start_play(type="server", encrypt=True):
	"""
	Stops and then starts the Play! services
	
	Args:
		type:
		Either "client" or "server" (defaults to "server"). This sets what
		Play! services to restart.
		
		encrypt:
		Only applies when type == "server". Defaults to True. If True, the
		encrypted partition will be remounted if it is not mounted.
	"""
	folder = get_folder(type)
	stop_play(type)
	if type == "server" and encrypt:
		mount_encrypted_partition()
			
	with cd("/src/%s" % folder):
		run("play start --%prod", pty=False)
		
	text("%s started!" % folder)
	
@task
def update_servers(type="server"):
	text("Updating apt package cache and packages")
	sudo("apt-get update -qq")
	sudo("apt-get upgrade -qqy")
	
	text("Updating source repositories")
	with cd("/src"):
		pull_or_clone()		
		
	text("Restarting play services")
	start_play(type)
	
@task
def update_configs(type="server", server_name="localhost", server_api_key="", encrypt=True):
	"""
	Updates the Play! and NGINX configs based on the supplied parameters
	Also restarts the Play! and NGINX services

	Args:
		type:
		Either "client" or "server" (defaults to "server"). This sets what
		Play! services to restart.
	
		server_name:
		defaults to "localhost". This is what is put in the webserver
		config file.
	
		server_api_key:
		Only applies when type is set to "server". This is written to the Play!
		config file and is used by the client to communicate with this server.

		encrypt:
		Only applies when type == "server". Defaults to True. If True, the
		encrypted partition will be remounted if it is not mounted.
	"""
	encrypt = str2bool(encrypt)
	text("""
Updating configs on server.

Type: %s
Server Name: %s
Server API Key: %s
Encrypt: %s""" % (type, server_name, server_api_key, encrypt))
	if not confirm("Is this okay?"):
		abort("Aborting on user request.")
		
	text("Stopping play servers and nginx")
	stop_play(type)
	sudo("service nginx stop", pty=False)
	text("Updating play and nginx configs")
	create_nginx_config(type, server_name)
	create_play_config(type, server_api_key, encrypt)
	text("Starting play servers and nginx")
	start_play(type, encrypt)
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
	
def pull_or_clone(type="server"):
	#note: assumes caller is using 'with cd("/src"):'
	for r in repositories:
		name = r[0]
		repo = r[1]
		
		#skip getting unnecessary repos
		if type == "server" and name == "openseedbox":
			continue
		if type == "client" and name == "openseedbox-server":
			continue
			
		if not exists(name):
			run("git clone -q %s /src/%s" % (repo, name))
		else:
			with cd("/src/%s" % name):
				#move application.conf elsewhere because otherwise there will be git pull conflicts
				moved_application_conf=False
				if exists("conf/application.conf"):
					run("mv conf/application.conf /tmp/%s.application.conf" % name)
					moved_application_conf=True
				run("git pull")
				if moved_application_conf:
					run("mv /tmp/%s.application.conf conf/application.conf" % name)
		with cd("/src/%s" % name):
			run("play deps --sync")
				
def mount_encrypted_partition():
	if not exists("/dev/mapper/openseedbox"):
		sudo("cryptsetup luksOpen ~/data.img openseedbox")
		sudo("mount /dev/mapper/openseedbox %s" % openseedbox_backend_path)

def create_nginx_config(type="server", servername=""):
	folder = get_folder(type)
	sudo("rm -fr /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default")
	if not servername:
		servername = prompt("What is this servers servername?")
	replace = get_config_params(servername)
	config = nginx_server_config if type == "server" else nginx_client_config
	sudo("rm -f /etc/nginx/sites-available/%s" % folder)
	put(StringIO(config % replace), "/etc/nginx/sites-available/%s" % folder, use_sudo=True)
	sudo("ln -fs /etc/nginx/sites-available/%s /etc/nginx/sites-enabled/%s" % (folder, folder))
	sudo("service nginx restart", pty=False)

def create_play_config(type="server", server_api_key="", encrypt=True):
	folder = get_folder(type)
	if (type == "server") and not server_api_key:
		server_api_key = prompt("What is this servers api_key?")
	replace = get_config_params(encrypt=encrypt)
	replace["api_key"] = server_api_key
	config = openseedbox_server_config if (type == "server")	else openseedbox_client_config
	put(StringIO(config % replace), "/src/%s/conf/application.conf" % folder)

def get_folder(type):
	return "openseedbox-server" if (type == "server") else "openseedbox"
	
def get_config_params(server_name="", encrypt=True):
	nginx_port = "443" if openseedbox_ssl_enabled else "80"
	return { "server_name" : server_name, "backend_port" : openseedbox_backend_port,
				"backend_path" : openseedbox_backend_path, "nginx_port" : nginx_port,
				"client_port" : openseedbox_client_port, "client_db_url" : openseedbox_client_db_url,
				"encrypted" : str(encrypt).lower() } 
	
def text(t):
	print(yellow(t))
	
def str2bool(v):
	if isinstance(v, bool):
		return v;
	return v.lower() in ("yes", "true", "t", "1")
  
