## To use the Ansible provisioning without using Vagrant or the Vagrantfile

- create a vagrant user with sudo priveleges
- install ansible
- login as vagrant
- git clone the repo https://github.com/rthomas320/FAF-Project to /home/vagrant
- cd /home/vagrant
- edit /home/vagrant/provisioning/setup_machine.yml change - hosts: all to - hosts: localhost
- anisble-playbook provisioning/setup_machine.yml
- source /home/vagrant/.bashrc [to set DISPLAY variable of VM to local machine for firefox to display]
- follow the instructions below
Note: The custom nifi_processors source files will be in /home/vagrant/nifi_processors instead
 of /home/vagrant/source/nifi_processors as in the OVA.
 The custom nifi processors use sftp to pass and retrieve files. The processor is configured to
 sftp to the localhost with 'root' as the user and 'vagrant' as the password.  The configuration
 for the Saniitzer1, Sanitizer2, Sanitizer3, and Analyzer1 processors may be changed to use
 a different user/password
---

## Manual NOE VM Steps to run and verify NiFi flow

To build custom NiFi processors and install the newly built nar file

- cd /home/vagrant/source/nifi_processors
- run: mvn clean install -Dmaven.test.skip=true
- sudo cp /home/vagrant/source/nifi_processors/target/siaft-processors.nar /opt/nifi-latest/lib/
- sudo chown nifi.nifi /opt/nifi-latest/lib/siaft-processors.nar
- sudo systemctl restart nifi

---

## To test the NiFi flow with the new or existing custom NiFi processors nar file

- vim /etc/ssh/sshd_config
- change PasswordAuthentication no to PasswordAuthentication yes
- systemctl restart sshd
- sudo passwd root
- su root
- cd /home/nifi
- ./setup-nifi-mock-env.sh
- cp /home/nifi/data/hold/\* /home/nifi/data/in/
- open a new terminal window
- log in as vagrant
- start firefox
- open browser window to https://localhost:8443/nifi
- cat /home/vagrant/nifi-creds.txt
- login into NiFi with the credentials from cat results
- save credentials in the popup screen
- in the Operate Nifi Flow click the last icon to upload the template file
- click select template
- find the template file in the /home/vagrant/Downloads directory, highlight it, click open button
- click the upload button
- click OK to close the dialog box
- click the and drag the template icon in the colored nifi menu to the nifi grid
- click add to add the template that was uploaded
- click a blank spot in the grid to un-highlight the processors and the relationships
- right click a blank spot in the grid and choose enable all controller services in the popup menu
- go back to the terminal window with root user in directory /home/nifi
- ./start-nifi-mock-processors.sh
- return to nifi flow in the firefox browser
- right click a blank area of the grid and select start from the popup menu
- right click a blank area of the grid and select refresh
- repeat refreshes until the three sample files are processed
  [Write Analyzer Results to The Database show 3 in and 3 out]
- right click a blank area of the grid select stop
- go to the terminal window with root user in directory /home/nifi
- sudo ./stop-nifi-mock-processors.sh
- exit [root]
- mysql -u vagrant -pvagrant siaft [to enter database commandline prompt]
- select \* from FileAttributes;
- select \* from Sanitize;
- select \* from Analysis;
- quit [to exit DB commandline prompt]
- sudo cat /home/nifi/data/out/some.txt to see s1, s2, s3 at the bottom of the file
  [indicating the file went through sanitizer1, sanitizer2, and sanitizer3]
- sudo cat /home/nifi/data/out/somemore.txt see s1, s2, s3 at the bottom of the file
- sudo cat /home/nifi/data/out/evenmore.txt see s1, s2, s3 at the bottom of the file

---

## Vagrant VMs

- Rocky9 (arm/aarch supported)

  - Easy way

    ```bash
    ./vagrant.sh -a up -r "9" # default is rocky8
    ```

  - Manual way:

    - Not arm/aarch platforms:

      ```bash
      export ROCKY_VERSION="9"
      vagrant up
      ```
    
    - arm/aarch platforms:

      ```bash
      export ROCKY_VERSION="9"
      export OS_ARCH=arm64
      vagrant up
      ```

- Rocky8 (not arm/aarch supported)

  - Easy way:

    ```bash
    ./vagrant.sh -a up
    ```

  - Manual way:
  
    ```bash
    vagrant up
    ```
