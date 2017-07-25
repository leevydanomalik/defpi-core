// declare a new module called 'myApp', and make it require the `ng-admin` module as a dependency
var myApp = angular.module('myApp', [ 'ng-admin' ]);
// declare a function to run when the module bootstraps (during the 'config'
// phase)

myApp.config(function(RestangularProvider) {
	var username = window.localStorage.getItem('efpi_orchestrator_username');
	var password = window.localStorage.getItem('efpi_orchestrator_password');

	var token = window.btoa(username + ':' + password);
	RestangularProvider.setDefaultHeaders({
		'Accept' : 'application/json',
		'Authorization' : 'Basic ' + token
	});
});

myApp
		.config([
				'NgAdminConfigurationProvider',
				function(nga) {
					var admin = nga.application('EF-Pi Orchestrator')
							.baseApiUrl('http://localhost:8080/');

					// Entities
					var user = nga.entity('user');
					var service = nga.entity('service');
					var intface = nga.entity('interface');
					var process = nga.entity('process');
					var connection = nga.entity('connection');
					var nodepool = nga.entity('nodepool');
					var puno = nga.entity('publicnode').label('Public Nodes');
					var prno = nga.entity('privatenode').label('Private Nodes');
					var un = nga.entity('unidentifiednode').label(
							'Unidentified Nodes').readOnly();

					// Views

					user.listView().fields(
							[ nga.field('username').isDetailLink(true),
									nga.field('email', 'email'),
									nga.field('admin', 'boolean') ]);

					user.creationView().fields(
							[ nga.field('username'),
									nga.field('email', 'email'),
									nga.field('password', 'password'),
									nga.field('admin', 'boolean').choices([ {
										value : true,
										label : 'true'
									}, {
										value : false,
										label : 'false'
									} ]) ]);
					user.editionView().fields(
							[ nga.field('username').editable(false),
									nga.field('email', 'email'),
									nga.field('password', 'password'),
									nga.field('admin', 'boolean').choices([ {
										value : true,
										label : 'true'
									}, {
										value : false,
										label : 'false'
									} ]) ]);

					process.listView().fields(
							[ nga.field('id'), nga.field('state'),
									nga.field('userId'),
									nga.field('serviceId'),
									nga.field('nodePoolId'),
									nga.field('privateNodeId'),
									nga.field('dockerId'),
									nga.field('runningDockerNodeId') ]);

					process.creationView().fields(
							[
									nga.field('userId', 'reference')
											.targetEntity(user).targetField(
													nga.field('username'))
											.label('User'),
									nga.field('serviceId', 'reference')
											.targetEntity(service).targetField(
													nga.field('id')).label(
													'Service'),
									nga.field('nodePoolId', 'reference')
											.targetEntity(nodepool)
											.targetField(nga.field('name'))
											.label('NodePool'),
									nga.field('privateNodeId', 'reference')
											.targetEntity(prno).targetField(
													nga.field('dockerId'))
											.label('PrivateNode') ]);

					service.listView().fields(
							[ nga.field('id').isDetailLink(true),
									nga.field('name'), nga.field('version'),
									nga.field('created', 'datetime') ]);

					service
							.showView()
							.fields(
									[
											nga.field('id'),
											nga.field('name'),
											nga.field('version'),
											nga.field('created', 'datetime'),
											nga
													.field('interfaces',
															'embedded_list')
													.targetFields(
															[
																	nga
																			.field('name'),
																	nga
																			.field(
																					'allowMultiple',
																					'boolean'),
																	nga
																			.field(
																					'autoConnect',
																					'boolean') ]) ]);

					intface.listView().fields(
							[
									nga.field('id'),
									nga.field('name'),
									nga.field('serviceId', 'reference')
											.targetEntity(service).targetField(
													nga.field('id')).label(
													'Service') ]);

					intface
							.showView()
							.fields(
									[
											nga.field('id'),
											nga.field('name'),
											nga.field('serviceId', 'reference')
													.targetEntity(service)
													.targetField(
															nga.field('id'))
													.label('Service'),
											nga.field('allowMultiple',
													'boolean'),
											nga.field('autoConnect', 'boolean'),
											nga
													.field('interfaceVersions',
															'embedded_list')
													.targetFields(
															[
																	nga
																			.field('versionName'),
																	nga
																			.field('sendsHash'),
																	nga
																			.field('receivesHash') ]) ]);

					connection.listView().fields(
							[
									nga.field('id'),
									nga.field('process1Id', 'reference')
											.targetEntity(process).targetField(
													nga.field('id')).label(
													'Process 1'),
									nga.field('interface1Id', 'reference')
											.targetEntity(intface).targetField(
													nga.field('id')).label(
													'Interface 1'),
									nga.field('process2Id', 'reference')
											.targetEntity(process).targetField(
													nga.field('id')).label(
													'Process 2'),
									nga.field('interface2Id', 'reference')
											.targetEntity(intface).targetField(
													nga.field('id')).label(
													'Interface 2') ]);
					connection.creationView().fields(
							[
									nga.field('process1Id', 'reference')
											.targetEntity(process).targetField(
													nga.field('id')).label(
													'Process 1'),
									nga.field('interface1Id', 'reference')
											.targetEntity(intface).targetField(
													nga.field('id')).label(
													'Interface 1'),
									nga.field('process2Id', 'reference')
											.targetEntity(process).targetField(
													nga.field('id')).label(
													'Process 2'),
									nga.field('interface2Id', 'reference')
											.targetEntity(intface).targetField(
													nga.field('id')).label(
													'Interface 2') ]);

					nodepool.listView().fields(
							[ nga.field('name').isDetailLink(true) ]);

					nodepool.creationView().fields(nga.field('name'));

					nodepool.editionView().fields(nga.field('name'));

					puno.listView().fields(
							[
									nga.field('id').isDetailLink(true),
									nga.field('dockerId'),
									nga.field('hostname'),
									nga.field('architecture'),
									nga.field('status'),
									nga.field('lastSync', 'datetime'),
									nga.field('nodePoolId', 'reference')
											.targetEntity(nodepool)
											.targetField(nga.field('name'))
											.label('Nodepool') ]);

					puno.showView().fields(puno.listView().fields());

					puno.creationView().fields(
							[
									nga.field('dockerId', 'reference')
											.targetEntity(un).targetField(
													nga.field('dockerId'))
											.label('Unidentified Node')
											.validation({
												required : true
											}),
									nga.field('nodePoolId', 'reference')
											.targetEntity(nodepool)
											.targetField(nga.field('name'))
											.label('NodePool').validation({
												required : true
											}) ]);

					prno.listView().fields(
							[
									nga.field('id').isDetailLink(true),
									nga.field('dockerId'),
									nga.field('hostname'),
									nga.field('architecture'),
									nga.field('status'),
									nga.field('lastSync', 'datetime'),
									nga.field('userId', 'reference')
											.targetEntity(user).targetField(
													nga.field('username'))
											.label('User') ]);

					prno.showView().fields(prno.listView().fields());

					prno.creationView().fields(
							[
									nga.field('dockerId', 'reference')
											.targetEntity(un).targetField(
													nga.field('dockerId'))
											.label('Unidentified Node')
											.validation({
												required : true
											}),
									nga.field('userId', 'reference')
											.targetEntity(user).targetField(
													nga.field('username'))
											.label('User').validation({
												required : true
											}) ]);

					un.listView().fields(
							[ nga.field('id').isDetailLink(true),
									nga.field('dockerId'),
									nga.field('hostname'),
									nga.field('architecture'),
									nga.field('status'),
									nga.field('lastSync', 'datetime') ]);

					// Add entities
					admin.addEntity(user);
					admin.addEntity(service);
					admin.addEntity(intface);
					admin.addEntity(process);
					admin.addEntity(connection);
					admin.addEntity(nodepool);
					admin.addEntity(un);
					admin.addEntity(puno);
					admin.addEntity(prno);

					// Menu
					admin
							.menu(nga
									.menu()
									.addChild(
											nga
													.menu(user)
													.icon(
															'<span class="glyphicon glyphicon-user"></span>'))
									.addChild(
											nga
													.menu(service)
													.icon(
															'<span class="glyphicon glyphicon-play-circle"></span>'))
									.addChild(
											nga
													.menu(intface)
													.icon(
															'<span class="glyphicon glyphicon-flash"></span>'))
									.addChild(
											nga
													.menu(process)
													.icon(
															'<span class="glyphicon glyphicon-repeat"></span>'))
									.addChild(
											nga
													.menu(connection)
													.icon(
															'<span class="glyphicon glyphicon-resize-small"></span>'))
									.addChild(
											nga
													.menu(nodepool)
													.icon(
															'<span class="glyphicon glyphicon-cloud"></span>'))
									.addChild(
											nga
													.menu(puno)
													.icon(
															'<span class="glyphicon glyphicon-hdd"></span>'))
									.addChild(
											nga
													.menu(prno)
													.icon(
															'<span class="glyphicon glyphicon-hdd"></span>'))
									.addChild(
											nga
													.menu(un)
													.icon(
															'<span class="glyphicon glyphicon-hdd"></span>')));

					var customHeaderTemplate = '<div class="navbar-header"><button type="button" class="navbar-toggle"ng-click="isCollapsed = !isCollapsed"><span class="icon-bar"></span> <span class="icon-bar"></span> <spanclass="icon-bar"></span></button><a class="navbar-brand" href="#" ng-click="appController.displayHome()">EF-Pi Orchestrator</a></div><ul class="nav navbar-top-links navbar-right hidden-xs"><li><a href="#" onclick="logout()"><iclass="fa fa-sign-out fa-fw"></i>Logout</a></li></ul>';

					admin.header(customHeaderTemplate);

					nga.configure(admin);
				} ]);