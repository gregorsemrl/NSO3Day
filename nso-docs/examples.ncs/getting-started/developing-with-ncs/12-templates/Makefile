all:
	$(MAKE) -C packages/router/src all
	$(MAKE) netsim

netsim:
	ncs-netsim create-network ./packages/router 3 ex --dir ./netsim


clean:
	for f in packages/*/src; do \
		$(MAKE) -C $$f clean || exit 1; \
	done
	rm -rf ./netsim  logs/* state/* ncs-cdb/*.cdb _tmp_ncs_cmd*
