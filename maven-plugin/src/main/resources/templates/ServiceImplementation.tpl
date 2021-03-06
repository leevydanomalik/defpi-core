package {{service.package}};

import java.io.Serializable;

import javax.annotation.Generated;

import org.flexiblepower.service.DefPiParameters;
import org.flexiblepower.service.Service;

/**
 * {{service.class}} provides an implementation of the {{service.name}} service
 *
 * File generated by org.flexiblepower.create-service-maven-plugin. 
 * NOTE: This file is generated as a stub, and has to be implemented by the user. Re-running the codegen plugin will
 * 		 not change the contents of this file.
 * Template by FAN, 2017
 * 
 * @author {{username}}
 */
@Generated(value = "{{generator}}", date = "{{date}}")
public class {{service.class}} implements Service<{{config.interface}}> {

	@Override
	public void resumeFrom(Serializable state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init({{config.interface}} config, DefPiParameters parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modify({{config.interface}} config) {
		// TODO Auto-generated method stub
	}

	@Override
	public Serializable suspend() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
	}
	
}
