package {{service.package}};

import java.io.Serializable;
import java.util.Properties;

import javax.annotation.Generated;

import org.flexiblepower.service.Service;

/**
 * {{service.class}} provides an implementation of the {{service.name}} service
 *
 * File generated by org.flexiblepower.create-service-maven-plugin. 
 * NOTE: This file is generated as a stub, and has to be implemented by the user. Re-running the codegen plugin will
 * 		 not change the contents of this file.
 * Template by TNO, 2017
 * 
 * @author {{username}}
 */
@Generated(value = "{{generator}}", date = "{{date}}")
public class {{service.class}} implements Service {
		
	@Override
	public void resumeFrom(Serializable state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(Properties props) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void modify(Properties props) {
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
