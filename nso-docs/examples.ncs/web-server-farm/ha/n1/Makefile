
start:
	NCS_IPC_PORT=5757 sname=n1 NCS_HA_NODE=n1 ncs -c ncs.conf
start-i:
	NCS_IPC_PORT=5757 sname=n1 NCS_HA_NODE=n1 ncs -c ncs.conf -i \
	--with-package-reload

stop:
	NCS_IPC_PORT=5757 sname=n1 NCS_HA_NODE=n1 ncs --stop
cli:
	NCS_IPC_PORT=5757 ncs_cli -u admin
