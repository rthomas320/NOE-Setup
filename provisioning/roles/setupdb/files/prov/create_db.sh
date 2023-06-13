#!/bin/bash
sudo mysql < /opt/nifi/db_scripts/create_db.sql
sudo mysql siaft < /opt/nifi/db_scripts/create_tables.sql
sudo mysql siaft < /opt/nifi/db_scripts/add_auto_increment_to_tables.sql
sudo mysql siaft < /opt/nifi/db_scripts/load_data.sql
