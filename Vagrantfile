# -*- mode: ruby -*-
# Install any missing plugins
required_plugins = %w( vagrant-vbguest vagrant-parallels vagrant-env vagrant-proxyconf )
plugins_to_install = required_plugins.select { |plugin| not Vagrant.has_plugin? plugin }
if not plugins_to_install.empty?
  puts "Installing plugins: #{plugins_to_install.join(' ')}"
  if system "vagrant plugin install #{plugins_to_install.join(' ')}"
    exec "vagrant #{ARGV.join(' ')}"
  else
    abort "Installation of one or more plugins has failed. Aborting."
  end
end

Vagrant.configure("2") do |config|
  os_arch = ENV['OS_ARCH'] || "x86_64"
  rocky_version = ENV['ROCKY_VERSION'] || "8"
  el_version = "el#{rocky_version}"

  # determine box to use based os arch and rocky version. arm/aarch only support on rocky9 not rocky8
  if (os_arch == "x86_64" ||  os_arch == "amd64") && rocky_version == "8" then
    config.vm.box = "roboxes/rocky8"
    rocky_version = 8
  elsif (os_arch == "x86_64" ||  os_arch == "amd64") && rocky_version == "9" then
    config.vm.box = "roboxes/rocky9"
  else
    config.vm.box = "luminositylabsllc/rockylinux9-aarch64"
    rocky_version = 9
    el_version = "el#{rocky_version}"
  end

  config.vm.hostname = "noe-open-rocky#{rocky_version}"

  config.trigger.after [:provision] do |t|
    t.name = "Reboot after provisioning"
    t.run = { :inline => "vagrant reload" }
  end

  # config.vm.network "forwarded_port", guest: 22, host: 2222
  # config.ssh.forward_agent = true
  # config.ssh.forward_x11 = true

  config.vm.network "forwarded_port", guest: 80, host: 8082

  config.vm.network "forwarded_port", guest: 8080, host: 8083
  config.vm.network "forwarded_port", guest: 8443, host: 8444

  config.env.enable

  # proxy config
  config.proxy.enabled = ENV['http_proxy'] ? (ENV['https_proxy'] ? true : false) : false
  config.proxy.http = ENV['http_proxy']
  config.proxy.https = ENV['https_proxy']
  config.proxy.no_proxy = "localhost,127.0.0.1"

  # Setup options for virtualbox
  config.vm.provider "virtualbox" do |vb|
    # config.vm.synced_folder ".", "/vagrant", type: "virtualbox" # seems to break stuff
    vb.name = "noe-open-rocky#{rocky_version}"
    vb.memory = "4096"
    vb.cpus = "2"
    config.vbguest.auto_update = false
  end

  # Setup options for parallels
  config.vm.provider "parallels" do |prl|
    # config.vm.synced_folder ".", "/vagrant", type: "parallels" # seems to break stuff
    prl.name = "noe-open-rocky#{rocky_version}"
    prl.memory = "4096"
    prl.cpus = "2"
    prl.check_guest_tools = true
    prl.update_guest_tools = true
  end

  # arm/aarch rocky9 box has two bad repos that cause issues if not removed
  if os_arch == "arm64" || os_arch == "aarch64" then
    config.vm.provision "shell" do |shell|
      shell.inline = "sudo rm -rf /etc/yum.repos.d/{docker-ce.repo,nodesource-el9.repo}"
    end
  end
  
  # run ansible playbook to config vm
  config.vm.provision "ansible" do |ansible|
    ansible.compatibility_mode = "2.0"
    ansible.verbose = "v"
    ansible.playbook = "provisioning/setup_machine.yml"
    ansible.extra_vars = {
      el_version: "#{el_version}"
    }
  end
end
