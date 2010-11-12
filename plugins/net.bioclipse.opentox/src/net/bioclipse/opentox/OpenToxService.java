package net.bioclipse.opentox;

public class OpenToxService {

	private String name;
	private String service;
	private String serviceSPARQL;

	
	public OpenToxService(String name, String service, String serviceSPARQL) {
		super();
		this.name = name;
		this.service = service;
		this.serviceSPARQL = serviceSPARQL;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	@Override
	public String toString() {
		return "OpenToxService [name=" + name + ", service=" + service
				+ ", serviceSPARQL=" + serviceSPARQL + "]";
	}
	
	/**
	 * A simple equals on name level
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OpenToxService) {
			OpenToxService in = (OpenToxService) obj;
			if (this.name.equals(in.name))
				return true;
		}
		return false;
	}

}
