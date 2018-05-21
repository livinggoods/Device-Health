package org.goods.living.tech.health.device.models;

public class Result<D> {

	boolean status;
	String message;
	D data;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}

	public Result(boolean status, String message, D data) {
		this.status = status;
		this.message = message;
		this.data = data;

	}

	public Result() {

	}

}
