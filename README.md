# NIL 3-Day Virtual NSO Training Course!

![image](https://user-images.githubusercontent.com/42440315/185983097-9bd99f35-64d1-4b2b-85f4-53526dc215c7.png)


Hello and welcome! This repository contains initial environment setup for the 3 day NSO custom training.

It includes the running instance of NSO with the pre-populated database and NEDs required to spin up the NSO public sandbox environment ready for the custom course.

To set up the environment properly, please follow the following steps:

1. Reserve your Cisco Network Services Orchestrator Sandbox instance: https://devnetsandbox.cisco.com/RM/Diagram/Index/43964e62-a13c-4929-bde7-a2f68ad6b27c?diagramType=Topology 
1. VPN to your Cisco Network Services Orchestrator Sandbox instance. Find the **Devbox Jumphost**. ![image](https://user-images.githubusercontent.com/42440315/185972634-127edcb7-109f-4db0-8ae1-d4aaf95186f5.png)

2. Login to the **DevBox Jumphost** (10.10.20.50) in your Cisco Network Services Orchestrator Sandbox instance and open the terminal application. You can use RDP, SSH or Visual Studio Code from your local environment. _Note: If using RDP, open the Terminal window application from your desktop_.![image](https://user-images.githubusercontent.com/42440315/185970146-245ada71-a96b-46a4-9f01-7912f3857470.png)

3. In your terminal, go to your home folder by issuing the command: **cd**
4. Issue the following command: **git clone https://github.com/gregorsemrl/NSO3Day.git nso-run** ![image](https://user-images.githubusercontent.com/42440315/185971335-f5d3bc32-ad70-4f2a-a61f-d2c68df7a3f2.png)
5. After the repository is successfully cloned, go to the nso-run folder: **cd nso-run** ![image](https://user-images.githubusercontent.com/42440315/185971764-3647caf4-d4ba-4b7d-ad9d-c08d84baf9b1.png)
6. Unzip the file ncs-cdb.zip: **unzip ncs-cdb.zip** ![image](https://user-images.githubusercontent.com/42440315/185971977-ac249e9b-97c9-4033-b626-e1680b5d5538.png)
7. Your environment in now ready to start the Discovery 1.

## Links to Discovery Guide

* **Discovery 1**: [Use Cisco NSO for the First Time](https://github.com/gregorsemrl/NSO3Day/blob/master/docs/NSO%203%20Day%20Custom%20Course%20-%20Discovery%201.pdf)
* **Discovery 2**: [Manage Device Inventory](https://github.com/gregorsemrl/NSO3Day/blob/master/docs/NSO%203%20Day%20Custom%20Course%20-%20Discovery%202.pdf)
* **Discovery 3**: [Configure and Troubleshoot Devices](https://github.com/gregorsemrl/NSO3Day/blob/master/docs/NSO%203%20Day%20Custom%20Course%20-%20Discovery%203.pdf)
* **Discovery 4**: [Create a Service Package](https://github.com/gregorsemrl/NSO3Day/blob/master/docs/NSO%203%20Day%20Custom%20Course%20-%20Discovery%204.pdf)
* Discovery 5: Use Cisco NSO RESTCONF API with Postman
