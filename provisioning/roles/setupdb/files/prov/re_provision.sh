 #!/bin/bash

echo "Stopping NiFi"
systemctl stop nifi

read -r -p "Clear database? [Y/n]" response
response=${response,,} #Lowers case
if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
	echo "Clearing database"
	mysql siaft < /opt/nifi/db_scripts/delete_data.sql
fi

read -r -p "Clear NiFi repositories? [Y/n]" response
response=${response,,} #Lowers case
if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
	echo "Clearing NiFi repositories"
	rm -rf /opt/nifi-latest/content_repository/*
	rm -rf /opt/nifi-latest/database_repository/*
	rm -rf /opt/nifi-latest/flowfile_repository/*
	rm -rf /opt/nifi-latest/provenance_repository/*
fi

read -r -p "Delete files from /home/nifi/data directories? [Y/n]" response
response=${response,,} #Lowers case
if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
	echo "Deleting files from /home/nifi/data/ subdirectories"

	read -r -p "Delete files from /home/nifi/data/attachments/? [Y/n]" response
	response=${response,,} #Lowers case
	if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
		echo "Deleting files from /home/nifi/data/attachments/"
		rm -rf /home/nifi/data/attachments/*
	fi

	read -r -p "Delete files from /home/nifi/data/error_landing_zone/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
		echo "Deleting files from /home/nifi/data/error_landing_zone/"
		rm -rf /home/nifi/data/error_landing_zone/*
	fi

	read -r -p "Delete files from /home/nifi/data/failure/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/failure/"
		rm -rf /home/nifi/data/failure/*
	fi

	read -r -p "Delete files from /home/nifi/data/in/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/in/"
		rm -rf /home/nifi/data/in/*
	fi

	read -r -p "Delete files from /home/nifi/data/original/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/original/"
		rm -rf /home/nifi/data/original/*
	fi

	read -r -p "Delete files from /home/nifi/data/out/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/out/"
		rm -rf /home/nifi/data/out/*
	fi

	read -r -p "Delete files from /home/nifi/data/pst/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/pst/"
		rm -rf /home/nifi/data/pst/*
	fi

	read -r -p "Delete files from /home/nifi/data/reports/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/reports/"
		rm -rf /home/nifi/data/reports/*
	fi

	read -r -p "Delete files from /home/nifi/data/toMaec/? [Y/n]" response
        response=${response,,} #Lowers case
        if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
                echo "Deleting files from /home/nifi/data/toMaec/"
		rm -rf /home/nifi/data/toMaec/*
	fi


fi

read -r -p "Start NiFi? [Y/n]" response
response=${response,,} #Lowers case
if [[ $response =~ ^(yes|y| ) ]] || [[ -z $response ]]; then
	echo "Starting NiFi"
	systemctl start nifi
fi
