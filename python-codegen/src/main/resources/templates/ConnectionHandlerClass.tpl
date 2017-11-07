from . import {{vitf.handler.interface}}


class {{vitf.handler.class}}({{vitf.handler.interface}}):
    """ {{vitf.handler.class}}
        Handles incoming messages from dEF-Pi connections
        Generated by {{generator}} at {{date}} by {{username}}

        NOTE: This file is generated as a stub, and has to be implemented by the user. Re-running the codegen plugin 
        will not change the contents of this file.

        Template by FAN, 2017 """
 
    def __init__(self, service, connection):
        """ Auto-generated constructor building the manager for the provided service """
        self.__service = service
        self.__connection = connection

    def on_suspend(self):
        """ Called when the connection is suspended
            Auto-generated method stub """

    def resume_after_suspend(self):
        """ Called when the connection is being suspended
            Auto-generated method stub """
        
    def on_interrupt(self):
        """ Called when the connection is interrupted
            Auto-generated method stub """

    def resume_after_interrupt(self):
        """ Called when the connection is restored after an interruption
            Auto-generated method stub """

    def terminated(self):
        """ Called when the connection is terminated
            Auto-generated method stub """

{{vitf.handler.implementations}}
