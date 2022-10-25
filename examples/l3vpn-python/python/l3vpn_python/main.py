# -*- mode: python; python-indent: 4 -*-
import ncs
from ncs.application import Service


# ------------------------
# SERVICE CALLBACK EXAMPLE
# ------------------------
class ServiceCallbacks(Service):

    # The create() callback is invoked inside NCS FASTMAP and
    # must always exist.
    @Service.create
    def cb_create(self, tctx, root, service, proplist):
        self.log.info(f'Configuring: {service.vpn_name} - {service.vpn_id}')
        
        # define common variables
        vrf = f'vpn{service.vpn_id}'
        vrf_description = f'By NSO: L3VPN - {service.customer}'
        rd = f'1:{service.vpn_id}'
        asn_ip = f'1:{service.vpn_id}'

        for lnk in service.link:
            # calculate variables
            interface_ip = f'172.31.{lnk.link_id}.1'
            interface_desc = f'By NSO: L3VPN - {service.customer} - {lnk.link_name}'
            
            # apply to variables object
            vars = ncs.template.Variables()
            vars.add('PE-DEVICE', lnk.pe_device)
            vars.add('VRF', vrf)
            vars.add('VRF_DESCRIPTION', vrf_description)
            vars.add('RD', rd)
            vars.add('ASN_IP', asn_ip)
            vars.add('INTERFACE', lnk.interface)
            vars.add('INTERFACE_IP', interface_ip)
            vars.add('INTERFACE_DESC', interface_desc)

            # apply to common template
            template = ncs.template.Template(service)
            template.apply('l3vpn-common', vars)

            if lnk.routing_protocol == 'bgp':
                # calculate variables
                forwarding_address = f'172.31.{lnk.link_id}.2'

                # apply to variables object
                vars_bgp = ncs.template.Variables()
                vars_bgp.add('PE-DEVICE', lnk.pe_device)
                vars_bgp.add('VRF', vrf)
                vars_bgp.add('BGP_NEIGHBOR_IP', forwarding_address)
                
                # apply to template for bgp config
                template = ncs.template.Template(service)
                template.apply('l3vpn-bgp', vars_bgp)

            if lnk.routing_protocol == 'static':
                for route in lnk.static_route:
                    # calculate variables
                    forwarding_address = f'172.31.{lnk.link_id}.2'
                    
                    # apply to variables object
                    vars_static = ncs.template.Variables()
                    vars_static.add('PE-DEVICE', lnk.pe_device)
                    vars_static.add('VRF', vrf)
                    vars_static.add('STATIC_ROUTE_IP', route.prefix)
                    vars_static.add('STATIC_ROUTE_MASK', route.mask)
                    vars_static.add('STATIC_ROUTE_FWD_IP', forwarding_address)
                    
                    # apply to template for static routing
                    template = ncs.template.Template(service)
                    template.apply('l3vpn-static', vars_static)
        
        self.log.info(f'Configuring DONE: {service.vpn_name} - {service.vpn_id}')

    # The pre_modification() and post_modification() callbacks are optional,
    # and are invoked outside FASTMAP. pre_modification() is invoked before
    # create, update, or delete of the service, as indicated by the enum
    # ncs_service_operation op parameter. Conversely
    # post_modification() is invoked after create, update, or delete
    # of the service. These functions can be useful e.g. for
    # allocations that should be stored and existing also when the
    # service instance is removed.

    # @Service.pre_lock_create
    # def cb_pre_lock_create(self, tctx, root, service, proplist):
    #     self.log.info('Service plcreate(service=', service._path, ')')

    # @Service.pre_modification
    # def cb_pre_modification(self, tctx, op, kp, root, proplist):
    #     self.log.info('Service premod(service=', kp, ')')

    # @Service.post_modification
    # def cb_post_modification(self, tctx, op, kp, root, proplist):
    #     self.log.info('Service premod(service=', kp, ')')


# ---------------------------------------------
# COMPONENT THREAD THAT WILL BE STARTED BY NCS.
# ---------------------------------------------
class Main(ncs.application.Application):
    def setup(self):
        # The application class sets up logging for us. It is accessible
        # through 'self.log' and is a ncs.log.Log instance.
        self.log.info('Main RUNNING')

        # Service callbacks require a registration for a 'service point',
        # as specified in the corresponding data model.
        #
        self.register_service('l3vpn-python-servicepoint', ServiceCallbacks)

        # If we registered any callback(s) above, the Application class
        # took care of creating a daemon (related to the service/action point).

        # When this setup method is finished, all registrations are
        # considered done and the application is 'started'.

    def teardown(self):
        # When the application is finished (which would happen if NCS went
        # down, packages were reloaded or some error occurred) this teardown
        # method will be called.

        self.log.info('Main FINISHED')
