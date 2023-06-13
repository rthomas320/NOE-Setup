#!/bin/bash
sudo systemctl enable --now httpd
sudo firewall-cmd --add-portid={http,https} --permanent
sudo firewall-cmd --reload
