#!/usr/bin/env bash
set -eux

msg() {
  content="${1:?Message content not specified!}"
  printf "%s\n" "==> $content"
}

msg "Beginning Bake!"

export DEBIAN_FRONTEND=noninteractive

apt_install_opts="-q -y"
packages_list=/tmp/packages.lst
python_exec="python2.7"
ubuntu_version="$(lsb_release -s -i | tr '[:upper:]' '[:lower:]')-$(lsb_release -s -r)"

# Fragile Hack
# http://stackoverflow.com/questions/36896806/how-can-i-be-sure-a-freshly-started-vm-is-ready-for-provisioning
sleep 60

msg "Updating system..."
apt-get update && apt-get upgrade ${apt_install_opts}

msg "Add custom Ubuntu PPAs"
apt-get install software-properties-common ${apt_install_opts} # needed for the add-apt-repository command
add-apt-repository -y ppa:nginx/stable

apt-get update

if [ -s "${packages_list}" ]; then
    msg "Installing essential distribution packages..."
    apt-get install $(grep -vE "^\s*#" "$packages_list" | tr "\n" " ") ${apt_install_opts}
    rm -f "${packages_list}"
fi

msg "Disabling unattended-upgrades..."
sudo apt-get remove synaptic* unattended-upgrades -y
sudo apt-get purge synaptic*
sudo apt-get autoremove
sudo apt-get autoclean

cat << EOF > /etc/apt/apt.conf.d/10periodic
APT::Periodic::Update-Package-Lists "1";
APT::Periodic::Download-Upgradeable-Packages "0";
APT::Periodic::AutocleanInterval "7";
APT::Periodic::Unattended-Upgrade "0";
EOF

msg "Cleaning up..."
apt-get clean
apt-get autoremove
rm -f /var/lib/dpkg/lock
rm -f /var/cache/apt/archives/lock

msg "Shredding SSH keys..."
shred -u /etc/ssh/*_key /etc/ssh/*_key.pub

msg "Done Baking!"
