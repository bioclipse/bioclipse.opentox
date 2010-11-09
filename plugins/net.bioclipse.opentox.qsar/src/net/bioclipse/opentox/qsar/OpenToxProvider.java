package net.bioclipse.opentox.qsar;

/**
 * An OpenTox service is defined by a service endpoint and 
 * a SPARQL endpoint
 * 
 * @author ola
 *
 */
public class OpenToxProvider {
	
	public String id;
	public String name;
	public String service;
	public String serviceSPARQL;


	public OpenToxProvider(String id, String name, String service, String serviceSPARQL) {
		super();
		this.id = id;
		this.name = name;
		this.service = service;
		this.serviceSPARQL = serviceSPARQL;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getServiceSPARQL() {
		return serviceSPARQL;
	}
	public void setServiceSPARQL(String serviceSPARQL) {
		this.serviceSPARQL = serviceSPARQL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
