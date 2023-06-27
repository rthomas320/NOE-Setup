#!/bin/bash
sudo systemctl enable --now httpd
sudo systemctl enable --now firewalld
sudo firewall-cmd --add-portid={http,https} --permanent
sudo firewall-cmd --reload
